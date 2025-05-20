package com.example.myapplication.ui.gallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.AppDatabase;
import com.example.myapplication.Main_Screen;
import com.example.myapplication.PasswordHasher;
import com.example.myapplication.R;
import com.example.myapplication.User;
import com.example.myapplication.UserDAO;
import com.example.myapplication.databinding.FragmentGalleryBinding;

import java.lang.ref.WeakReference;

public class GalleryFragment extends Fragment {

    private static final String TAG = "GalleryFragment_Room"; // Tag cho log

    private FragmentGalleryBinding binding;
    private EditText edtName, edtPhone, edtAddress;
    private TextView tvUsername;
    private Button btnSave, btnChangePassword;

    private UserDAO userDao; // Thêm UserDAO
    private int currentLoggedInUserId = -1; // Lưu User ID đang đăng nhập
    private User currentUser; // Lưu object User đang xem/chỉnh sửa

    // ViewModel chỉ cần nếu bạn có logic UI đặc biệt, nếu không có thể xóa
    // private GalleryViewModel galleryViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class); // Giữ lại nếu dùng ViewModel

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Ánh xạ các View sử dụng binding
        edtName = binding.edtName;
        edtPhone = binding.edtPhone;
        edtAddress = binding.edtAddress;
        tvUsername = binding.tvUsername; // Tên đăng nhập không sửa
        btnSave = binding.btnSave;
        btnChangePassword = binding.btnChangePassword;
        TextView textViewTitle = binding.textGallery; // Tiêu đề

        // Khởi tạo UserDAO
        userDao = AppDatabase.getInstance(requireContext()).userDao();

        // Lấy User ID từ SharedPreferences (giống MainActivity)
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        currentLoggedInUserId = defaultPrefs.getInt(Main_Screen.KEY_LOGGED_IN_USER_ID_INT, -1);

        if (currentLoggedInUserId == -1) {
            Log.e(TAG, "Không tìm thấy User ID trong SharedPreferences. Không thể tải thông tin.");
            Toast.makeText(requireContext(), "Lỗi phiên đăng nhập. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            // Vô hiệu hóa các trường và nút nếu không có user
            disableUI();
            // TODO: Cân nhắc điều hướng về màn hình đăng nhập hoặc hiển thị màn hình trống/lỗi
        } else {
            Log.d(TAG, "Đã lấy được User ID: " + currentLoggedInUserId + " từ SharedPreferences.");
            loadUserInfoFromDatabase(currentLoggedInUserId); // Tải thông tin từ DB
            enableUI(); // Đảm bảo UI được bật nếu có user ID
        }

        // Xử lý sự kiện click nút Lưu thay đổi
        btnSave.setOnClickListener(v -> saveUserInfo());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Nếu bạn dùng ViewModel để set text cho textViewTitle
        /*
        galleryViewModel.getText().observe(getViewLifecycleOwner(), newText -> {
            textViewTitle.setText(newText);
        });
        */
        // Nếu không dùng ViewModel, có thể đặt text cố định (hoặc từ string resource như đã sửa layout)
        textViewTitle.setText(R.string.edit_account_title); // Sử dụng string resource đã thêm vào strings.xml


        return root;
    }

    // <<< THAY ĐỔI CHÍNH: Tải thông tin người dùng từ Database >>>
    private void loadUserInfoFromDatabase(int userId) {
        Log.d(TAG, "Bắt đầu tải thông tin user ID " + userId + " từ Database.");
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentUser = userDao.findById(userId); // Lấy User object từ DB

            if (currentUser != null) {
                Log.d(TAG, "Đã tìm thấy user ID " + userId + ". Tên: " + currentUser.getName() + ", Username: " + currentUser.getUsername());
                // Cập nhật UI trên luồng chính
                requireActivity().runOnUiThread(() -> {
                    edtName.setText(currentUser.getName());
                    tvUsername.setText(currentUser.getUsername()); // Tên đăng nhập không sửa
                    edtPhone.setText(currentUser.getPhone());
                    edtAddress.setText(currentUser.getAddress());
                    Log.i(TAG, "Đã cập nhật UI với thông tin user.");
                });
            } else {
                Log.e(TAG, "Không tìm thấy user với ID " + userId + " trong database.");
                // Cập nhật UI trên luồng chính
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin người dùng trong DB.", Toast.LENGTH_LONG).show();
                    disableUI(); // Vô hiệu hóa UI
                    // TODO: Cân nhắc điều hướng về màn hình đăng nhập
                });
            }
        });
    }

    // <<< THAY ĐỔI CHÍNH: Lưu thông tin người dùng vào Database >>>
    private void saveUserInfo() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu người dùng chưa được tải.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Không thể lưu thông tin: currentUser là null.");
            return;
        }

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        // Kiểm tra các trường bắt buộc (nếu cần, ví dụ tên không được trống)
        if (name.isEmpty()) {
            edtName.setError("Tên không được để trống");
            edtName.requestFocus();
            return;
        }

        // Cập nhật thông tin vào object User hiện tại
        currentUser.setName(name);
        currentUser.setPhone(phone.isEmpty() ? null : phone); // Lưu null nếu trống
        currentUser.setAddress(address.isEmpty() ? null : address); // Lưu null nếu trống

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                userDao.update(currentUser); // Cập nhật User object vào DB
                Log.i(TAG, "Đã lưu thông tin user ID " + currentUser.getId() + " vào database.");
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi cập nhật thông tin user ID " + currentUser.getId() + " vào database", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Lỗi khi lưu thông tin.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // <<< THAY ĐỔI CHÍNH: Logic đổi mật khẩu tương tác với Database >>>
    private void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu người dùng chưa được tải.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Không thể đổi mật khẩu: currentUser là null.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Xác nhận mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Đổi mật khẩu thất bại: Mật khẩu mới và xác nhận không khớp.");
            return;
        }
        if (newPassword.isEmpty()) { // Chỉ cần kiểm tra newPassword vì đã kiểm tra khớp
            Toast.makeText(requireContext(), "Mật khẩu mới không được để trống!", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Đổi mật khẩu thất bại: Mật khẩu mới bị trống.");
            return;
        }
        // Có thể thêm kiểm tra độ dài/độ phức tạp mật khẩu mới ở đây

        // Thực hiện xác thực mật khẩu cũ và cập nhật mật khẩu mới trong luồng nền
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean isPasswordCorrect = PasswordHasher.verifyPassword(oldPassword, currentUser.getHashedPassword());

            if (isPasswordCorrect) {
                // Băm mật khẩu mới
                byte[] salt = PasswordHasher.generateSalt();
                String newHashedPassword = PasswordHasher.hashPassword(newPassword, salt);

                if (newHashedPassword != null) {
                    currentUser.setHashedPassword(newHashedPassword); // Cập nhật mật khẩu đã băm vào User object
                    try {
                        userDao.update(currentUser); // Cập nhật User object vào DB
                        Log.i(TAG, "Đã đổi mật khẩu thành công cho user ID " + currentUser.getId());
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Mật khẩu đã được thay đổi!", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi cập nhật mật khẩu mới vào database cho user ID " + currentUser.getId(), e);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Lỗi khi lưu mật khẩu mới.", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.e(TAG, "Lỗi khi băm mật khẩu mới.");
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Lỗi nội bộ khi xử lý mật khẩu.", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                Log.w(TAG, "Đổi mật khẩu thất bại: Mật khẩu cũ không đúng cho user ID " + currentUser.getId());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.button_change_password);

        // Tạo layout cho dialog
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        final EditText oldPasswordInput = new EditText(requireContext());
        oldPasswordInput.setHint(R.string.hint_old_password);
        oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPasswordInput);

        final EditText newPasswordInput = new EditText(requireContext());
        newPasswordInput.setHint(R.string.hint_new_password);
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        final EditText confirmNewPasswordInput = new EditText(requireContext());
        confirmNewPasswordInput.setHint(R.string.hint_confirm_password);
        confirmNewPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmNewPasswordInput);

        builder.setView(layout);

        builder.setPositiveButton(R.string.button_change_password, (dialog, which) -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordInput.getText().toString().trim();
            changePassword(oldPassword, newPassword, confirmNewPassword);
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }


    // <<< THÊM HÀM TIỆN ÍCH ĐỂ BẬT/TẮT UI >>>
    private void disableUI() {
        edtName.setEnabled(false);
        edtPhone.setEnabled(false);
        edtAddress.setEnabled(false);
        btnSave.setEnabled(false);
        btnChangePassword.setEnabled(false);
        // tvUsername không cần disable vì nó đã có enabled="false" trong XML
    }

    private void enableUI() {
        edtName.setEnabled(true);
        edtPhone.setEnabled(true);
        edtAddress.setEnabled(true);
        btnSave.setEnabled(true);
        btnChangePassword.setEnabled(true);
    }
    // -------------------------------------


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        currentUser = null; // Giải phóng tham chiếu đến User object
    }
}