package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddBenhAnActivity extends AppCompatActivity {
    private static final String TAG = "AddBenhAnActivity";

    private EditText etDiagnosis, etMedicalHistory, etLabResults, etAllergies, etCurrentMedications, etDiseaseStage;
    private Button btnSave, btnGetAdvice; // Thêm nút mới
    private BenhAnDao benhAnDao;
    private int currentLoggedInUserId = -1;

    // PHẦN THÊM MỚI ĐỂ GỌI API
    private ApiService apiService;
    private ProgressDialog progressDialog;
    private static final String BASE_URL = "http://192.168.1.3:5000/"; // THAY ĐỊA CHỈ IP CỦA BẠN VÀO ĐÂY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_benh_an);

        // Ánh xạ các View
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etMedicalHistory = findViewById(R.id.etMedicalHistory);
        etLabResults = findViewById(R.id.etLabResults);
        etAllergies = findViewById(R.id.etAllergies);
        etCurrentMedications = findViewById(R.id.etCurrentMedications);
        etDiseaseStage = findViewById(R.id.etDiseaseStage);
        btnSave = findViewById(R.id.btnSave);
        btnGetAdvice = findViewById(R.id.btnGetAdvice); // Ánh xạ nút mới

        // Logic cũ để khởi tạo CSDL Room
        benhAnDao = AppDatabase.getInstance(this).benhAnDao();

        // Logic cũ để lấy User ID
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        currentLoggedInUserId = prefs.getInt(Main_Screen.KEY_LOGGED_IN_USER_ID_INT, -1);

        if (currentLoggedInUserId == -1) {
            Log.e(TAG, "Lỗi: Không lấy được User ID. Vô hiệu hóa các nút.");
            Toast.makeText(this, "Lỗi phiên đăng nhập. Không thể thực hiện.", Toast.LENGTH_LONG).show();
            btnSave.setEnabled(false);
            btnGetAdvice.setEnabled(false); // Vô hiệu hóa cả nút mới
        }

        // Logic cũ cho nút LƯU VÀO MÁY
        btnSave.setOnClickListener(view -> {
            if (currentLoggedInUserId != -1) {
                saveBenhAnToLocalDB();
            } else {
                Toast.makeText(AddBenhAnActivity.this, "Lỗi: Không xác định được người dùng.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- PHẦN LOGIC THÊM MỚI ---
        // 1. Khởi tạo Retrofit để gọi API
        setupApiService();

        // 2. Sự kiện cho nút mới: NHẬN TƯ VẤN AI
        btnGetAdvice.setOnClickListener(view -> {
            if (currentLoggedInUserId != -1) {
                getAdviceFromAI();
            } else {
                Toast.makeText(AddBenhAnActivity.this, "Lỗi: Không xác định được người dùng.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm lưu vào CSDL cục bộ (giữ nguyên, đổi tên cho rõ ràng)
    private void saveBenhAnToLocalDB() {
        String diagnosis = etDiagnosis.getText().toString().trim();
        if (diagnosis.isEmpty()) {
            etDiagnosis.setError("Chẩn đoán không được để trống");
            etDiagnosis.requestFocus();
            return;
        }

        BenhAn benhAn = new BenhAn();
        benhAn.setUserId(currentLoggedInUserId);
        benhAn.diagnosis = diagnosis;
        benhAn.medicalHistory = etMedicalHistory.getText().toString().trim();
        benhAn.labResults = etLabResults.getText().toString().trim();
        benhAn.allergies = etAllergies.getText().toString().trim();
        benhAn.currentMedications = etCurrentMedications.getText().toString().trim();
        benhAn.diseaseStage = etDiseaseStage.getText().toString().trim();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            benhAnDao.insert(benhAn);
            runOnUiThread(() -> {
                Toast.makeText(AddBenhAnActivity.this, "Đã lưu bệnh án vào máy", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    // --- CÁC HÀM MỚI ĐỂ GỌI API ---
    private void setupApiService() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void getAdviceFromAI() {
        String diagnosis = etDiagnosis.getText().toString().trim();
        if (diagnosis.isEmpty()) {
            etDiagnosis.setError("Chẩn đoán không được để trống để nhận tư vấn");
            etDiagnosis.requestFocus();
            return;
        }

        // Tạo đối tượng BenhAnRequest để gửi đi
        BenhAnRequest requestData = new BenhAnRequest(
                currentLoggedInUserId,
                diagnosis,
                etMedicalHistory.getText().toString().trim(),
                etLabResults.getText().toString().trim(),
                etAllergies.getText().toString().trim(),
                etCurrentMedications.getText().toString().trim(),
                etDiseaseStage.getText().toString().trim()
        );

        showProgressDialog("Đang kết nối tới AI", "Vui lòng chờ trong giây lát...");

        // Giả sử bạn đã có file ApiService, BenhAnRequest và AdviceResponse
        apiService.getAdviceFromRecord(requestData).enqueue(new Callback<AdviceResponse>() {
            @Override
            public void onResponse(Call<AdviceResponse> call, Response<AdviceResponse> response) {
                dismissProgressDialog();
                if (response.isSuccessful() && response.body() != null) {
                    String advice = response.body().getAdvice();

                    // Chuyển kết quả sang ResultActivity để hiển thị
                    Intent resultIntent = new Intent(AddBenhAnActivity.this, ResultActivity.class);

                    // Dòng gây lỗi đã được xóa.
                    // Bây giờ chỉ cần gửi nội dung lời khuyên. ResultActivity đã được nâng cấp để tự xử lý.
                    resultIntent.putExtra(ResultActivity.EXTRA_RESULT_TEXT, advice);

                    // Vì đây là luồng tư vấn từ bệnh án, không có kết quả OCR,
                    // nên ta không cần gửi `EXTRA_DRUG_LIST`. `ResultActivity` sẽ xử lý trường hợp này.

                    startActivity(resultIntent);

                } else {
                    Toast.makeText(AddBenhAnActivity.this, "Lỗi từ server: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AdviceResponse> call, Throwable t) {
                dismissProgressDialog();
                Toast.makeText(AddBenhAnActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm tiện ích cho ProgressDialog
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
}