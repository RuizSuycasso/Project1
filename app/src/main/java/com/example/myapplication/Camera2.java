// File: src/main/java/com/example/myapplication/Camera2.java
// << PHIÊN BẢN NÂNG CẤP: Tích hợp OCR + AI >>

package com.example.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Camera2 extends AppCompatActivity {

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ImageView imageView;
    private TextView resultTextView;
    private Uri imageUri;
    private ApiService apiService;
    private ProgressDialog progressDialog;

    private static final String BASE_URL = "http://192.168.1.3:5000/"; // THAY ĐỊA CHỈ IP CỦA BẠN
    private static final int CONNECT_TIMEOUT_SEC = 30;
    private static final int READ_TIMEOUT_SEC = 90; // Tăng thời gian chờ cho AI
    private static final int WRITE_TIMEOUT_SEC = 90;
    private static final int MAX_RETRIES = 2;
    private static final int IMAGE_MAX_SIZE = 1024;
    private static final int IMAGE_COMPRESSION_QUALITY = 75;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnLibrary = findViewById(R.id.btnLibrary);

        initializeLaunchers();
        btnCamera.setOnClickListener(v -> checkCameraPermission());
        btnLibrary.setOnClickListener(v -> openGallery());
    }

    private boolean isPureAscii(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches("^[\\x00-\\x7F]*$");
    }

    // =========================================================================
    // === THAY ĐỔI LOGIC CỐT LÕI: TÁCH THÀNH 2 BƯỚC API CALL             ===
    // =========================================================================

    /**
     * Bước 1: Chỉ upload ảnh để lấy danh sách thuốc (OCR)
     */
    private void startOcrProcess(MultipartBody.Part imagePart, File tempFile, final int retryCount) {
        showProgressDialog("Bước 1/2: Phân tích đơn thuốc", "Đang xử lý hình ảnh...");

        apiService.uploadImage(imagePart).enqueue(new Callback<List<DrugInfo>>() {
            @Override
            public void onResponse(Call<List<DrugInfo>> call, Response<List<DrugInfo>> response) {
                if (tempFile.exists()) tempFile.delete(); // Xóa file tạm ngay

                if (response.isSuccessful() && response.body() != null) {
                    List<DrugInfo> allDrugs = response.body();
                    ArrayList<DrugInfo> filteredDrugList = new ArrayList<>();

                    for (DrugInfo drug : allDrugs) {
                        String drugName = drug.getDrug();
                        if (drugName != null && !drugName.trim().isEmpty() &&
                                isPureAscii(drugName) &&
                                drugName.length() > 2 &&
                                Character.isLetter(drugName.charAt(0))) {
                            filteredDrugList.add(drug);
                        }
                    }

                    if (filteredDrugList.isEmpty()) {
                        dismissProgressDialog();
                        resultTextView.setText("Không tìm thấy thuốc hợp lệ trong đơn.");
                        return;
                    }

                    // OCR thành công, chuyển sang Bước 2: Lấy lời khuyên AI
                    getAiAdvice(filteredDrugList);

                } else {
                    dismissProgressDialog();
                    resultTextView.setText("Lỗi Server (OCR) - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<DrugInfo>> call, Throwable t) {
                if (tempFile.exists()) tempFile.delete();

                if (retryCount < MAX_RETRIES) {
                    int nextRetry = retryCount + 1;
                    resultTextView.setText("Kết nối thất bại. Đang thử lại lần " + nextRetry + "/" + MAX_RETRIES);
                    startOcrProcess(imagePart, tempFile, nextRetry);
                } else {
                    dismissProgressDialog();
                    resultTextView.setText("Lỗi mạng (OCR): " + t.getMessage());
                    t.printStackTrace();
                }
            }
        });
    }

    /**
     * Bước 2: Gửi thông tin thuốc đã OCR để lấy lời khuyên từ AI
     * @param drugList danh sách thuốc đã được lọc
     */
    private void getAiAdvice(ArrayList<DrugInfo> drugList) {
        showProgressDialog("Bước 2/2: Lấy tư vấn từ AI", "Đang gửi thông tin thuốc...");

        // Tạo một chuỗi chứa tên các thuốc
        String drugNames;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            drugNames = drugList.stream().map(DrugInfo::getDrug).collect(Collectors.joining(", "));
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < drugList.size(); i++) {
                sb.append(drugList.get(i).getDrug());
                if (i < drugList.size() - 1) {
                    sb.append(", ");
                }
            }
            drugNames = sb.toString();
        }

        // Tạo một đối tượng BenhAnRequest đơn giản chỉ với thông tin thuốc
        BenhAnRequest request = new BenhAnRequest(0, "Tư vấn dựa trên đơn thuốc", "", "", "", drugNames, "");

        apiService.getAdviceFromRecord(request).enqueue(new Callback<AdviceResponse>() {
            @Override
            public void onResponse(Call<AdviceResponse> call, Response<AdviceResponse> response) {
                dismissProgressDialog(); // Hoàn tất, ẩn dialog

                if (response.isSuccessful() && response.body() != null) {
                    String adviceText = response.body().getAdvice();

                    // ĐÃ CÓ CẢ HAI: danh sách thuốc và lời khuyên. Gửi cả hai sang ResultActivity.
                    launchResultActivity(drugList, adviceText);

                } else {
                    // Nếu lấy lời khuyên thất bại, vẫn hiển thị kết quả OCR
                    Toast.makeText(Camera2.this, "Không nhận được lời khuyên AI. Hiển thị kết quả OCR.", Toast.LENGTH_LONG).show();
                    launchResultActivity(drugList, null); // Gửi null cho lời khuyên
                }
            }

            @Override
            public void onFailure(Call<AdviceResponse> call, Throwable t) {
                dismissProgressDialog();
                // Nếu lấy lời khuyên thất bại, vẫn hiển thị kết quả OCR
                Toast.makeText(Camera2.this, "Lỗi kết nối AI. Hiển thị kết quả OCR.", Toast.LENGTH_LONG).show();
                launchResultActivity(drugList, null); // Gửi null cho lời khuyên
                t.printStackTrace();
            }
        });
    }

    /**
     * Hàm cuối cùng: Mở ResultActivity và gửi tất cả dữ liệu qua
     * @param drugList Danh sách thuốc
     * @param adviceText Lời khuyên AI (có thể null)
     */
    private void launchResultActivity(ArrayList<DrugInfo> drugList, String adviceText) {
        Intent intent = new Intent(Camera2.this, ResultActivity.class);
        intent.putParcelableArrayListExtra(ResultActivity.EXTRA_DRUG_LIST, drugList);
        intent.putExtra(ResultActivity.EXTRA_RESULT_TEXT, adviceText); // Dù null hay không vẫn gửi
        startActivity(intent);
    }


    // --- Các hàm còn lại giữ nguyên, không cần thay đổi ---
    private void initializeLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) openCamera();
                    else Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) startImageCrop(uri);
                    else Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
                }
        );

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) startImageCrop(imageUri);
                    else Toast.makeText(this, "Failed to capture image!", Toast.LENGTH_SHORT).show();
                }
        );

        cropImageLauncher = registerForActivityResult(
                new CropImageContract(),
                result -> {
                    if (result.isSuccessful()) {
                        Uri croppedUri = result.getUriContent();
                        if (croppedUri != null) {
                            imageView.setImageURI(croppedUri);
                            processImage(croppedUri);
                        }
                    } else {
                        Exception error = result.getError();
                        if (error != null) Toast.makeText(this, "Crop error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    // Hàm này bây giờ chỉ gọi bước đầu tiên của chuỗi xử lý
    private void processImage(Uri imageUri) {
        if (!isNetworkAvailable()) {
            resultTextView.setText("Không có kết nối Internet.");
            return;
        }

        resultTextView.setText("Đang chuẩn bị ảnh...");
        File imageFile = getCompressedAndResizedImageFile(imageUri);
        if (imageFile == null) {
            resultTextView.setText("Không thể chuẩn bị file ảnh.");
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        // Bắt đầu chuỗi xử lý bằng cách gọi OCR trước
        startOcrProcess(body, imageFile, 0);
    }

    private void checkCameraPermission() { requestPermissionLauncher.launch(Manifest.permission.CAMERA); }
    private void openGallery() { pickImageLauncher.launch("image/*"); }
    private void openCamera() {
        imageUri = createImageUri();
        if (imageUri != null) takePictureLauncher.launch(imageUri);
        else Toast.makeText(this, "Failed to create image URI!", Toast.LENGTH_SHORT).show();
    }
    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "photo_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
    private void startImageCrop(Uri imageUri) {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        CropImageContractOptions cropOptions = new CropImageContractOptions(imageUri, options);
        cropImageLauncher.launch(cropOptions);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
    private void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing()) progressDialog.show();
    }
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }
    private File getCompressedAndResizedImageFile(Uri uri) {
        File file = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        Bitmap bitmap = null;
        try {
            file = File.createTempFile("temp_compressed_image_", ".jpg", getCacheDir());
            inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
                int photoWidth = options.outWidth;
                int photoHeight = options.outHeight;
                int scaleFactor = Math.max(1, Math.min(photoWidth / IMAGE_MAX_SIZE, photoHeight / IMAGE_MAX_SIZE));
                options.inJustDecodeBounds = false;
                options.inSampleSize = scaleFactor;
                options.inMutable = true;
                inputStream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                if (bitmap == null) return null;
                int currentWidth = bitmap.getWidth();
                int currentHeight = bitmap.getHeight();
                if (currentWidth > IMAGE_MAX_SIZE || currentHeight > IMAGE_MAX_SIZE) {
                    float ratio = Math.min((float) IMAGE_MAX_SIZE / currentWidth, (float) IMAGE_MAX_SIZE / currentHeight);
                    Matrix matrix = new Matrix();
                    matrix.postScale(ratio, ratio);
                    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, currentWidth, currentHeight, matrix, true);
                    if (scaledBitmap != bitmap) {
                        bitmap.recycle();
                        bitmap = scaledBitmap;
                    }
                }
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_COMPRESSION_QUALITY, outputStream);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (file != null && file.exists()) file.delete();
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}