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

// Cần import AppDatabase
import com.example.myapplication.AppDatabase;

public class EditBenhAnActivity extends AppCompatActivity {

    public static final String EXTRA_BENHAN_ID = "com.example.myapplication.EXTRA_BENHAN_ID";
    private static final String TAG = "EditBenhAnActivity";

    private EditText etDiagnosis, etMedicalHistory, etLabResults, etAllergies, etCurrentMedications, etDiseaseStage;
    private Button btnUpdate, btnDelete;

    private BenhAnDao benhAnDao;
    private BenhAn currentBenhAn;
    private int benhAnId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_benhan);

        etDiagnosis = findViewById(R.id.etDiagnosisEdit);
        etMedicalHistory = findViewById(R.id.etMedicalHistoryEdit);
        etLabResults = findViewById(R.id.etLabResultsEdit);
        etAllergies = findViewById(R.id.etAllergiesEdit);
        etCurrentMedications = findViewById(R.id.etCurrentMedicationsEdit);
        etDiseaseStage = findViewById(R.id.etDiseaseStageEdit);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        benhAnDao = AppDatabase.getInstance(this).benhAnDao();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_BENHAN_ID)) {
            benhAnId = intent.getIntExtra(EXTRA_BENHAN_ID, -1);
        }

        if (benhAnId != -1) {
            // --- THAY ĐỔI: Tải dữ liệu trên background thread ---
            loadBenhAnDetails(benhAnId);
            // ------------------------------------------------
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID bệnh án.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "ID bệnh án không hợp lệ hoặc không được truyền qua Intent.");
            finish();
            return;
        }

        btnUpdate.setOnClickListener(view -> {
            updateBenhAn();
        });

        btnDelete.setOnClickListener(view -> {
            showDeleteConfirmationDialog();
        });

        // Tạm thời đặt tiêu đề ở đây, sẽ cập nhật lại sau khi load xong
        setTitle("Chỉnh sửa Bệnh án...");
    }

    private void loadBenhAnDetails(int id) {
        // --- THAY ĐỔI: Thực thi trên background thread ---
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentBenhAn = benhAnDao.getBenhAnById(id);

            // Quay lại Main thread để cập nhật UI
            runOnUiThread(() -> {
                if (currentBenhAn != null) {
                    // --- QUAN TRỌNG: Gán dữ liệu cho TẤT CẢ các trường ---
                    etDiagnosis.setText(currentBenhAn.diagnosis);
                    etMedicalHistory.setText(currentBenhAn.medicalHistory);
                    etLabResults.setText(currentBenhAn.labResults);       // Đảm bảo gán
                    etAllergies.setText(currentBenhAn.allergies);        // Đảm bảo gán
                    etCurrentMedications.setText(currentBenhAn.currentMedications); // Đảm bảo gán
                    etDiseaseStage.setText(currentBenhAn.diseaseStage);      // Đảm bảo gán
                    // -----------------------------------------------------
                    setTitle("Chỉnh sửa Bệnh án ID: " + currentBenhAn.id); // Cập nhật tiêu đề đúng
                } else {
                    Toast.makeText(EditBenhAnActivity.this, "Lỗi: Không tìm thấy bệnh án trong cơ sở dữ liệu.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Không tìm thấy bệnh án với ID = " + id);
                    finish();
                }
            });
        });
        // ---------------------------------------------
    }

    private void updateBenhAn() {
        // Lấy dữ liệu mới từ EditText
        String diagnosis = etDiagnosis.getText().toString().trim(); // Thêm trim()
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

        // Phải đảm bảo currentBenhAn đã được load thành công
        if (currentBenhAn != null) {
            // Cập nhật đối tượng currentBenhAn (giữ nguyên ID)
            currentBenhAn.diagnosis = diagnosis;
            currentBenhAn.medicalHistory = medicalHistory;
            currentBenhAn.labResults = labResults;
            currentBenhAn.allergies = allergies;
            currentBenhAn.currentMedications = currentMedications;
            currentBenhAn.diseaseStage = diseaseStage;

            // --- THAY ĐỔI: Thực hiện update trên background thread ---
            AppDatabase.databaseWriteExecutor.execute(() -> {
                benhAnDao.update(currentBenhAn);
                // Quay lại Main thread để thông báo và đóng Activity
                runOnUiThread(() -> {
                    Toast.makeText(EditBenhAnActivity.this, "Đã cập nhật bệnh án", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
            // -------------------------------------------------------
        } else {
            Toast.makeText(this, "Lỗi: Không thể cập nhật, dữ liệu bệnh án chưa được tải.", Toast.LENGTH_SHORT).show();
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
                .setMessage("Bạn có chắc chắn muốn xóa bệnh án ID " + currentBenhAn.id + " không?") // Thêm ID vào thông báo
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Có, Xóa", (dialog, whichButton) -> {
                    // --- THAY ĐỔI: Thực hiện delete trên background thread ---
                    deleteCurrentBenhAn();
                    // ----------------------------------------------------
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void deleteCurrentBenhAn() {
        if (currentBenhAn != null) {
            // --- THAY ĐỔI: Thực hiện delete trên background thread ---
            AppDatabase.databaseWriteExecutor.execute(() -> {
                benhAnDao.delete(currentBenhAn);
                // Quay lại Main thread để thông báo và đóng Activity
                runOnUiThread(() -> {
                    Toast.makeText(EditBenhAnActivity.this, "Đã xóa bệnh án", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
            // -------------------------------------------------------
        } else {
            Toast.makeText(this, "Lỗi: Không thể xóa bệnh án.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "currentBenhAn là null khi cố gắng xóa.");
        }
    }
}