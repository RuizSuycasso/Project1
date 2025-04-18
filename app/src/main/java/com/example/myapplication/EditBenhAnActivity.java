package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EditBenhAnActivity extends AppCompatActivity {

    // Hằng số để lấy ID từ Intent, nên là public static final
    public static final String EXTRA_BENHAN_ID = "com.example.myapplication.EXTRA_BENHAN_ID";
    private static final String TAG = "EditBenhAnActivity"; // Tag cho Log

    private EditText etDiagnosis, etMedicalHistory, etLabResults, etAllergies, etCurrentMedications, etDiseaseStage;
    private Button btnUpdate, btnDelete;

    private BenhAnDao benhAnDao;
    private BenhAn currentBenhAn; // Lưu trữ bệnh án đang sửa
    private int benhAnId = -1; // ID của bệnh án, -1 nếu không hợp lệ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_benhan);

        // Ánh xạ Views (sử dụng ID mới từ activity_edit_benhan.xml)
        etDiagnosis = findViewById(R.id.etDiagnosisEdit);
        etMedicalHistory = findViewById(R.id.etMedicalHistoryEdit);
        etLabResults = findViewById(R.id.etLabResultsEdit);
        etAllergies = findViewById(R.id.etAllergiesEdit);
        etCurrentMedications = findViewById(R.id.etCurrentMedicationsEdit);
        etDiseaseStage = findViewById(R.id.etDiseaseStageEdit);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        benhAnDao = AppDatabase.getInstance(this).benhAnDao();

        // Lấy ID từ Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_BENHAN_ID)) {
            benhAnId = intent.getIntExtra(EXTRA_BENHAN_ID, -1);
        }

        // Kiểm tra ID hợp lệ và tải dữ liệu
        if (benhAnId != -1) {
            // --- CẢNH BÁO: Thao tác DB trên luồng chính ---
            loadBenhAnDetails(benhAnId);
            // -------------------------------------------
        } else {
            // ID không hợp lệ, không thể sửa
            Toast.makeText(this, "Lỗi: Không tìm thấy ID bệnh án.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "ID bệnh án không hợp lệ hoặc không được truyền qua Intent.");
            finish(); // Đóng activity nếu không có ID
            return; // Dừng thực thi onCreate
        }

        // Xử lý nút Cập nhật
        btnUpdate.setOnClickListener(view -> {
            updateBenhAn();
        });

        // Xử lý nút Xóa
        btnDelete.setOnClickListener(view -> {
            showDeleteConfirmationDialog();
        });

        // Thay đổi tiêu đề Activity
        setTitle("Chỉnh sửa Bệnh án ID: " + benhAnId);
    }

    private void loadBenhAnDetails(int id) {
        // --- CẢNH BÁO: Thao tác DB trên luồng chính ---
        currentBenhAn = benhAnDao.getBenhAnById(id);
        // -------------------------------------------

        if (currentBenhAn != null) {
            // Điền dữ liệu vào EditText
            etDiagnosis.setText(currentBenhAn.diagnosis);
            etMedicalHistory.setText(currentBenhAn.medicalHistory);
            etLabResults.setText(currentBenhAn.labResults);
            etAllergies.setText(currentBenhAn.allergies);
            etCurrentMedications.setText(currentBenhAn.currentMedications);
            etDiseaseStage.setText(currentBenhAn.diseaseStage);
        } else {
            // Không tìm thấy bệnh án với ID này trong DB
            Toast.makeText(this, "Lỗi: Không tìm thấy bệnh án trong cơ sở dữ liệu.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Không tìm thấy bệnh án với ID = " + id);
            finish(); // Đóng activity
        }
    }

    private void updateBenhAn() {
        // Lấy dữ liệu mới từ EditText
        String diagnosis = etDiagnosis.getText().toString();
        String medicalHistory = etMedicalHistory.getText().toString();
        String labResults = etLabResults.getText().toString();
        String allergies = etAllergies.getText().toString();
        String currentMedications = etCurrentMedications.getText().toString();
        String diseaseStage = etDiseaseStage.getText().toString();

        // Kiểm tra dữ liệu cơ bản (ví dụ: chẩn đoán không được trống)
        if (diagnosis.trim().isEmpty()) {
            etDiagnosis.setError("Chẩn đoán không được để trống");
            etDiagnosis.requestFocus();
            return;
        }

        // Cập nhật đối tượng currentBenhAn (Quan trọng: giữ nguyên ID)
        if (currentBenhAn != null) {
            currentBenhAn.diagnosis = diagnosis;
            currentBenhAn.medicalHistory = medicalHistory;
            currentBenhAn.labResults = labResults;
            currentBenhAn.allergies = allergies;
            currentBenhAn.currentMedications = currentMedications;
            currentBenhAn.diseaseStage = diseaseStage;

            // --- CẢNH BÁO: Thao tác DB trên luồng chính ---
            benhAnDao.update(currentBenhAn);
            // -------------------------------------------

            Toast.makeText(this, "Đã cập nhật bệnh án", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity sau khi cập nhật thành công
        } else {
            Toast.makeText(this, "Lỗi: Không thể cập nhật bệnh án.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "currentBenhAn là null khi cố gắng cập nhật.");
        }
    }

    private void showDeleteConfirmationDialog() {
        if (currentBenhAn == null) {
            Toast.makeText(this, "Lỗi: Không có bệnh án để xóa.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bệnh án này không?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Có, Xóa", (dialog, whichButton) -> {
                    // --- CẢNH BÁO: Thao tác DB trên luồng chính ---
                    deleteCurrentBenhAn();
                    // -------------------------------------------
                })
                .setNegativeButton("Không", null) // Không làm gì cả nếu chọn "Không"
                .show();
    }

    private void deleteCurrentBenhAn() {
        if (currentBenhAn != null) {
            // --- CẢNH BÁO: Thao tác DB trên luồng chính ---
            benhAnDao.delete(currentBenhAn);
            // -------------------------------------------
            Toast.makeText(this, "Đã xóa bệnh án", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity sau khi xóa
        } else {
            Toast.makeText(this, "Lỗi: Không thể xóa bệnh án.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "currentBenhAn là null khi cố gắng xóa.");
        }
    }
}