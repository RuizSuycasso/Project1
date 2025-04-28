package com.example.myapplication;

import android.Manifest;
import android.content.ContentValues;
// import android.database.Cursor; // Không dùng
import android.graphics.Bitmap;
// import android.graphics.BitmapFactory; // Không dùng
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
// Không cần import bất cứ thứ gì của Chaquopy nữa
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

// import java.io.ByteArrayOutputStream; // Có vẻ không dùng, có thể xóa

public class Camera2 extends AppCompatActivity {

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private ImageView imageView;
    private TextView resultTextView;
    private Uri imageUri;
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        // Không còn gọi initPython() nữa

        // Initialize ML Kit Text Recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Initialize views
        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnLibrary = findViewById(R.id.btnLibrary);

        // Initialize launchers
        initializeLaunchers();

        // Set click listeners
        btnCamera.setOnClickListener(v -> checkCameraPermission());
        btnLibrary.setOnClickListener(v -> openGallery());
    }

    // Không cần hàm initPython() nữa

    private void initializeLaunchers() {
        // Camera permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Gallery launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        startImageCrop(uri);
                    } else {
                        Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Camera launcher
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        startImageCrop(imageUri);
                    } else {
                        Toast.makeText(this, "Failed to capture image!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Crop launcher
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
                        if (error != null) {
                            Toast.makeText(this, "Crop error: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void checkCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void openCamera() {
        imageUri = createImageUri();
        if (imageUri != null) {
            takePictureLauncher.launch(imageUri);
        } else {
            Toast.makeText(this, "Failed to create image URI!", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "photo_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    private void startImageCrop(Uri imageUri) {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        // Bạn có thể điều chỉnh hoặc xóa bỏ các tùy chọn cắt ảnh nếu muốn
        // options.aspectRatioX = 1;
        // options.aspectRatioY = 1;
        options.outputCompressFormat = Bitmap.CompressFormat.PNG; // Hoặc JPEG
        options.outputCompressQuality = 90; // Chất lượng ảnh nén

        CropImageContractOptions cropOptions = new CropImageContractOptions(imageUri, options);
        cropImageLauncher.launch(cropOptions);
    }

    private void processImage(Uri imageUri) {
        try {
            // Sử dụng try-with-resources sẽ tốt hơn nếu làm việc với InputStream
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            if (bitmap != null) {
                InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
                textRecognizer.process(inputImage)
                        .addOnSuccessListener(text -> {
                            String recognizedText = text.getText();
                            resultTextView.setText(recognizedText); // Hiển thị kết quả OCR
                            Toast.makeText(Camera2.this, "OCR successful!", Toast.LENGTH_SHORT).show();
                            // Có thể bạn muốn làm gì đó khác với recognizedText ở đây
                        })
                        .addOnFailureListener(e -> {
                            resultTextView.setText("OCR Error: " + e.getMessage()); // Hiển thị lỗi
                            Toast.makeText(Camera2.this, "OCR error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                resultTextView.setText("Could not decode bitmap from URI.");
                Toast.makeText(this, "Could not decode bitmap", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            resultTextView.setText("Processing Error: " + e.getMessage()); // Hiển thị lỗi
            Toast.makeText(this, "Image processing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}