package com.example.myapplication;

import android.content.SharedPreferences; // <<< THÊM
import android.os.Bundle;
import android.preference.PreferenceManager; // <<< THÊM
import android.util.Log; // <<< THÊM
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
// Import AppDatabase đã được thực hiện ngầm

public class AddBenhAnActivity extends AppCompatActivity {
    private static final String TAG = "AddBenhAnActivity"; // Tag cho log

    EditText etDiagnosis, etMedicalHistory, etLabResults, etAllergies, etCurrentMedications, etDiseaseStage;
    Button btnSave;
    private BenhAnDao benhAnDao;
    private int currentLoggedInUserId = -1; // Biến để lưu ID người dùng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_benh_an);

        etDiagnosis = findViewById(R.id.etDiagnosis);
        etMedicalHistory = findViewById(R.id.etMedicalHistory);
        etLabResults = findViewById(R.id.etLabResults);
        etAllergies = findViewById(R.id.etAllergies);
        etCurrentMedications = findViewById(R.id.etCurrentMedications);
        etDiseaseStage = findViewById(R.id.etDiseaseStage);
        btnSave = findViewById(R.id.btnSave);

        // <<< THAY ĐỔI CHÍNH: Sử dụng AppDatabase >>>
        benhAnDao = AppDatabase.getInstance(this).benhAnDao();

        // <<< THÊM: Lấy User ID từ SharedPreferences >>>
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Key này PHẢI khớp với key bạn đã dùng trong Main_Screen.java để lưu ID (INT)
        currentLoggedInUserId = prefs.getInt(Main_Screen.KEY_LOGGED_IN_USER_ID_INT, -1);

        if (currentLoggedInUserId == -1) {
            Log.e(TAG, "Lỗi: Không lấy được User ID. Không thể thêm bệnh án.");
            Toast.makeText(this, "Lỗi phiên đăng nhập. Không thể thêm bệnh án.", Toast.LENGTH_LONG).show();
            btnSave.setEnabled(false); // Vô hiệu hóa nút lưu
        }

        btnSave.setOnClickListener(view -> {
            if (currentLoggedInUserId != -1) { // Chỉ lưu nếu có User ID hợp lệ
                saveBenhAn();
            } else {
                Toast.makeText(AddBenhAnActivity.this, "Lỗi: Không xác định được người dùng.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveBenhAn() {
        String diagnosis = etDiagnosis.getText().toString().trim();
        String medicalHistory = etMedicalHistory.getText().toString().trim();
        String labResults = etLabResults.getText().toString().trim();
        String allergies = etAllergies.getText().toString().trim();
        String currentMedications = etCurrentMedications.getText().toString().trim();
        String diseaseStage = etDiseaseStage.getText().toString().trim();

        if (diagnosis.isEmpty()) {
            etDiagnosis.setError("Chẩn đoán không được để trống");
            etDiagnosis.requestFocus();
            return;
        }

        BenhAn benhAn = new BenhAn();
        benhAn.setUserId(currentLoggedInUserId); // <<< THAY ĐỔI CHÍNH: GÁN USER ID CHO BỆNH ÁN
        benhAn.diagnosis = diagnosis;
        benhAn.medicalHistory = medicalHistory;
        benhAn.labResults = labResults;
        benhAn.allergies = allergies;
        benhAn.currentMedications = currentMedications;
        benhAn.diseaseStage = diseaseStage;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            benhAnDao.insert(benhAn);
            runOnUiThread(() -> {
                Toast.makeText(AddBenhAnActivity.this, "Đã thêm bệnh án", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}