package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
// Import cần thiết
import com.example.myapplication.BenhAn;
import com.example.myapplication.AppDatabase; // Cần import AppDatabase

public class AddBenhAnActivity extends AppCompatActivity {

    EditText etDiagnosis, etMedicalHistory, etLabResults, etAllergies, etCurrentMedications, etDiseaseStage;
    Button btnSave;
    private BenhAnDao benhAnDao; // Thêm biến DAO

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

        // Khởi tạo DAO
        benhAnDao = AppDatabase.getInstance(this).benhAnDao();

        btnSave.setOnClickListener(view -> {
            saveBenhAn(); // Gọi hàm lưu mới
        });
    }

    private void saveBenhAn() {
        // Lấy dữ liệu từ EditText
        String diagnosis = etDiagnosis.getText().toString().trim(); // Thêm trim()
        String medicalHistory = etMedicalHistory.getText().toString().trim();
        String labResults = etLabResults.getText().toString().trim();
        String allergies = etAllergies.getText().toString().trim();
        String currentMedications = etCurrentMedications.getText().toString().trim();
        String diseaseStage = etDiseaseStage.getText().toString().trim();

        // Kiểm tra dữ liệu cơ bản (ví dụ: chẩn đoán không được trống)
        if (diagnosis.isEmpty()) {
            etDiagnosis.setError("Chẩn đoán không được để trống");
            etDiagnosis.requestFocus();
            return;
        }

        BenhAn benhAn = new BenhAn();
        benhAn.diagnosis = diagnosis;
        benhAn.medicalHistory = medicalHistory;
        benhAn.labResults = labResults;
        benhAn.allergies = allergies;
        benhAn.currentMedications = currentMedications;
        benhAn.diseaseStage = diseaseStage;

        // --- THAY ĐỔI: Thực hiện insert trên background thread ---
        AppDatabase.databaseWriteExecutor.execute(() -> {
            benhAnDao.insert(benhAn);
            // Quay lại Main thread để hiển thị Toast và đóng Activity
            runOnUiThread(() -> {
                Toast.makeText(AddBenhAnActivity.this, "Đã thêm bệnh án", Toast.LENGTH_SHORT).show();
                finish(); // Đóng activity sau khi lưu thành công
            });
        });
        // ------------------------------------------------------
    }
}