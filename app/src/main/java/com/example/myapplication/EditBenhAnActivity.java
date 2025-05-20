package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences; // <<< THÊM IMPORT
import android.os.Bundle;
import android.preference.PreferenceManager; // <<< THÊM IMPORT
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// AppDatabase đã được import ngầm

public class EditBenhAnActivity extends AppCompatActivity {

    public static final String EXTRA_BENHAN_ID = "com.example.myapplication.EXTRA_BENHAN_ID"; // Giữ nguyên key này
    private static final String TAG = "EditBenhAnActivity";

    private EditText etDiagnosis, etMedicalHistory, etLabResults, etAllergies, etCurrentMedications, etDiseaseStage;
    private Button btnUpdate, btnDelete;

    private BenhAnDao benhAnDao;
    private BenhAn currentBenhAn;
    private int benhAnId = -1;
    private int loggedInUserId = -1; // <<< THÊM: Để lưu ID người dùng đang đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_benhan); // Đảm bảo layout này tồn tại và có các ID đúng

        etDiagnosis = findViewById(R.id.etDiagnosisEdit);
        etMedicalHistory = findViewById(R.id.etMedicalHistoryEdit);
        etLabResults = findViewById(R.id.etLabResultsEdit);
        etAllergies = findViewById(R.id.etAllergiesEdit);
        etCurrentMedications = findViewById(R.id.etCurrentMedicationsEdit);
        etDiseaseStage = findViewById(R.id.etDiseaseStageEdit);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        benhAnDao = AppDatabase.getInstance(this).benhAnDao();

        // <<< THÊM: Lấy ID người dùng đang đăng nhập >>>
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Key này PHẢI khớp với key bạn đã dùng trong Main_Screen.java để lưu ID (INT)
        loggedInUserId = prefs.getInt(Main_Screen.KEY_LOGGED_IN_USER_ID_INT, -1);

        // --- THÊM KIỂM TRA NGAY LÚC START ---
        if (loggedInUserId == -1) {
            Log.e(TAG, "loggedInUserId không hợp lệ khi khởi động Activity. Đang đóng Activity.");
            Toast.makeText(this, "Lỗi phiên đăng nhập. Không thể chỉnh sửa.", Toast.LENGTH_LONG).show();
            // Vô hiệu hóa các trường và nút (chỉ để đảm bảo, dù Activity sẽ finish)
            disableUI();
            finish(); // Nếu không có người dùng đăng nhập, không cho phép chỉnh sửa -> đóng Activity
            return; // Thoát khỏi onCreate
        }
        // ------------------------------------
        Log.d(TAG, "User ID đang đăng nhập (từ SharedPreferences): " + loggedInUserId);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_BENHAN_ID)) {
            benhAnId = intent.getIntExtra(EXTRA_BENHAN_ID, -1);
        }

        // --- THÊM KIỂM TRA benhAnId NGAY LÚC START ---
        if (benhAnId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID bệnh án để chỉnh sửa.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "ID bệnh án không hợp lệ hoặc không được truyền qua Intent. Đang đóng Activity.");
            disableUI();
            finish(); // Nếu không có ID bệnh án, không cho phép chỉnh sửa -> đóng Activity
            return; // Thoát khỏi onCreate
        }
        // --------------------------------------------
        Log.d(TAG, "ID bệnh án nhận được qua Intent: " + benhAnId);


        // Tải chi tiết bệnh án nếu cả loggedInUserId và benhAnId đều hợp lệ
        loadBenhAnDetails(benhAnId);


        btnUpdate.setOnClickListener(view -> {
            // Kiểm tra quyền trước khi cập nhật (dù đã kiểm tra lúc load)
            if (currentBenhAn != null && currentBenhAn.getUserId() == loggedInUserId) {
                updateBenhAn();
            } else {
                Toast.makeText(this, "Bạn không có quyền cập nhật bệnh án này.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Ngăn cập nhật: Kiểm tra quyền thất bại. BenhAn UserId: " + (currentBenhAn != null ? currentBenhAn.getUserId() : "N/A") + ", LoggedIn UserId: " + loggedInUserId);
            }
        });

        btnDelete.setOnClickListener(view -> {
            // Kiểm tra quyền trước khi xóa (dù đã kiểm tra lúc load)
            if (currentBenhAn != null && currentBenhAn.getUserId() == loggedInUserId) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(this, "Bạn không có quyền xóa bệnh án này.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Ngăn xóa: Kiểm tra quyền thất bại. BenhAn UserId: " + (currentBenhAn != null ? currentBenhAn.getUserId() : "N/A") + ", LoggedIn UserId: " + loggedInUserId);
            }
        });

        // setTitle("Chỉnh sửa Bệnh án..."); // Sẽ được cập nhật sau khi loadBenhAnDetails
    }

    private void loadBenhAnDetails(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentBenhAn = benhAnDao.getBenhAnById(id);

            runOnUiThread(() -> {
                if (currentBenhAn != null) {
                    Log.d(TAG, "Đã tải bệnh án ID " + id + ", UserID của bệnh án: " + currentBenhAn.getUserId());
                    // <<< THÊM: KIỂM TRA QUYỀN SỞ HỮU >>>
                    if (currentBenhAn.getUserId() == loggedInUserId) {
                        Log.i(TAG, "Kiểm tra quyền thành công. User ID " + loggedInUserId + " có quyền truy cập bệnh án ID " + id);
                        etDiagnosis.setText(currentBenhAn.diagnosis);
                        etMedicalHistory.setText(currentBenhAn.medicalHistory);
                        etLabResults.setText(currentBenhAn.labResults);
                        etAllergies.setText(currentBenhAn.allergies);
                        etCurrentMedications.setText(currentBenhAn.currentMedications);
                        etDiseaseStage.setText(currentBenhAn.diseaseStage);
                        setTitle("Chỉnh sửa Bệnh án ID: " + currentBenhAn.id);
                        btnUpdate.setEnabled(true); // Cho phép cập nhật
                        btnDelete.setEnabled(true); // Cho phép xóa
                        enableUI(); // Bật UI nếu đã bị disable
                    } else {
                        // Người dùng không sở hữu bệnh án này
                        Toast.makeText(EditBenhAnActivity.this, "Bạn không có quyền xem hoặc chỉnh sửa bệnh án này.", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Từ chối truy cập: User ID " + loggedInUserId + " cố gắng truy cập bệnh án ID " + id + " của User ID " + currentBenhAn.getUserId());
                        // Vô hiệu hóa các trường và nút
                        disableUI();
                        setTitle("Không có quyền truy cập");
                        // <<< THAY ĐỔI QUAN TRỌNG: Đóng Activity nếu không có quyền truy cập >>>
                        finish(); // Thoát khỏi Activity ngay lập tức
                        // -------------------------------------------------------------------
                    }
                } else {
                    Toast.makeText(EditBenhAnActivity.this, "Lỗi: Không tìm thấy bệnh án trong cơ sở dữ liệu.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Không tìm thấy bệnh án với ID = " + id + " trong DB.");
                    disableUI(); // Vô hiệu hóa UI
                    finish(); // Đóng Activity nếu không tìm thấy bệnh án
                }
            });
        });
    }

    private void updateBenhAn() {
        String diagnosis = etDiagnosis.getText().toString().trim();
        String medicalHistory = etMedicalHistory.getText().toString().trim();
        String labResults = etLabResults.getText().toString().trim();
        String allergies = etAllergies.getText().toString().trim();
        String currentMedications = etCurrentMedications.getText().toString().trim();
        String diseaseStage = etDiseaseStage.getText().toString().toString().trim();

        if (diagnosis.isEmpty()) {
            etDiagnosis.setError("Chẩn đoán không được để trống");
            etDiagnosis.requestFocus();
            return;
        }

        if (currentBenhAn != null) {
            // Không cần thay đổi currentBenhAn.userId vì nó đã đúng và không được sửa trên UI
            currentBenhAn.diagnosis = diagnosis;
            currentBenhAn.medicalHistory = medicalHistory;
            currentBenhAn.labResults = labResults;
            currentBenhAn.allergies = allergies;
            currentBenhAn.currentMedications = currentMedications;
            currentBenhAn.diseaseStage = diseaseStage;

            AppDatabase.databaseWriteExecutor.execute(() -> {
                benhAnDao.update(currentBenhAn);
                runOnUiThread(() -> {
                    Toast.makeText(EditBenhAnActivity.this, "Đã cập nhật bệnh án", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            Toast.makeText(this, "Lỗi: Không thể cập nhật, dữ liệu bệnh án chưa được tải hoặc đã bị mất.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "currentBenhAn là null khi cố gắng cập nhật.");
        }
    }

    private void showDeleteConfirmationDialog() {
        // currentBenhAn đã được kiểm tra quyền sở hữu trước khi gọi hàm này (trong setOnClickListener)
        // và cũng đã được kiểm tra not null
        if (currentBenhAn == null) { // Kiểm tra an toàn lần nữa
            Toast.makeText(this, "Lỗi: Không có bệnh án để xóa.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "currentBenhAn là null khi hiển thị dialog xóa.");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bệnh án ID " + currentBenhAn.id + " không?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Có, Xóa", (dialog, whichButton) -> {
                    deleteCurrentBenhAn();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void deleteCurrentBenhAn() {
        // currentBenhAn đã được kiểm tra quyền sở hữu và not null
        if (currentBenhAn != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                benhAnDao.delete(currentBenhAn);
                runOnUiThread(() -> {
                    Toast.makeText(EditBenhAnActivity.this, "Đã xóa bệnh án", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            Toast.makeText(this, "Lỗi: Không thể xóa bệnh án.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "currentBenhAn là null khi cố gắng xóa.");
        }
    }

    // <<< THÊM HÀM TIỆN ÍCH ĐỂ BẬT/TẮT UI >>>
    private void disableUI() {
        etDiagnosis.setEnabled(false);
        etMedicalHistory.setEnabled(false);
        etLabResults.setEnabled(false);
        etAllergies.setEnabled(false);
        etCurrentMedications.setEnabled(false);
        etDiseaseStage.setEnabled(false);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    private void enableUI() {
        etDiagnosis.setEnabled(true);
        etMedicalHistory.setEnabled(true);
        etLabResults.setEnabled(true);
        etAllergies.setEnabled(true);
        etCurrentMedications.setEnabled(true);
        etDiseaseStage.setEnabled(true);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
    }
    // -------------------------------------
}