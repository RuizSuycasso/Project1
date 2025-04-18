package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
// Không cần import AppDatabase cụ thể nếu dùng qua Room class
// import com.example.myapplication.room.AppDatabase; // Có thể bỏ nếu gọi AppDatabase.getInstance trực tiếp
import com.example.myapplication.BenhAn; // Đảm bảo import BenhAn

public class AddBenhAnActivity extends AppCompatActivity {

    EditText etDiagnosis, etMedicalHistory, etLabResults, etAllergies, etCurrentMedications, etDiseaseStage;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_benh_an); // Đảm bảo tên layout khớp với file XML

        etDiagnosis = findViewById(R.id.etDiagnosis);
        etMedicalHistory = findViewById(R.id.etMedicalHistory);
        etLabResults = findViewById(R.id.etLabResults);
        etAllergies = findViewById(R.id.etAllergies);
        etCurrentMedications = findViewById(R.id.etCurrentMedications);
        etDiseaseStage = findViewById(R.id.etDiseaseStage);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(view -> {
            // Lấy dữ liệu từ EditText
            String diagnosis = etDiagnosis.getText().toString();
            String medicalHistory = etMedicalHistory.getText().toString();
            String labResults = etLabResults.getText().toString();
            String allergies = etAllergies.getText().toString();
            String currentMedications = etCurrentMedications.getText().toString();
            String diseaseStage = etDiseaseStage.getText().toString();

            // Có thể thêm kiểm tra dữ liệu đầu vào ở đây (ví dụ: không để trống trường bắt buộc)

            BenhAn benhAn = new BenhAn();
            benhAn.diagnosis = diagnosis;
            benhAn.medicalHistory = medicalHistory;
            benhAn.labResults = labResults;
            benhAn.allergies = allergies;
            benhAn.currentMedications = currentMedications;
            benhAn.diseaseStage = diseaseStage;

            // --- CẢNH BÁO: Thao tác cơ sở dữ liệu trên luồng chính ---
            // Dòng code dưới đây thực hiện việc ghi vào DB trên Main Thread.
            // Điều này được phép do .allowMainThreadQueries() trong AppDatabase,
            // nhưng có thể gây treo UI (ANR) nếu thao tác chậm.
            // Nên sử dụng Coroutines, AsyncTask, RxJava,... để thực hiện bất đồng bộ.
            AppDatabase.getInstance(this).benhAnDao().insert(benhAn);
            // ----------------------------------------------------------

            Toast.makeText(this, "Đã thêm bệnh án", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity sau khi lưu
        });
    }
}