package com.example.myapplication; // Đảm bảo package đúng

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask; // *** THÊM AsyncTask ***
import android.os.Bundle;
import android.preference.PreferenceManager; // *** THÊM PreferenceManager ***
import android.util.Log; // *** THÊM Log ***
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.ref.WeakReference; // *** THÊM WeakReference ***

public class Main_Screen extends AppCompatActivity {

    private static final String TAG = "MainScreen_Login_Room"; // Đổi Tag
    public static final String KEY_CURRENT_USER_ID = "current_user_username"; // Key cho SharedPreferences

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen);

        // --- Ánh xạ Views ---
        usernameInput = findViewById(R.id.password); // ID "password" dùng cho username? Nên đổi ID
        passwordInput = findViewById(R.id.password2); // ID "password2" dùng cho password? Nên đổi ID
        loginButton = findViewById(R.id.button);
        registerButton = findViewById(R.id.button3);

        // --- Chuyển sang trang đăng ký ---
        registerButton.setOnClickListener(p -> {
            Intent intent = new Intent(Main_Screen.this, Main_Screen2.class);
            startActivity(intent);
        });

        // --- Điều chỉnh padding cho Edge-to-Edge ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- XỬ LÝ SỰ KIỆN NÚT ĐĂNG NHẬP (SỬ DỤNG ROOM VÀ ASYNCTASK) ---
        loginButton.setOnClickListener(p -> {
            String inputUser = usernameInput.getText().toString().trim();
            String inputPass = passwordInput.getText().toString(); // Lấy mật khẩu gốc

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                Toast.makeText(Main_Screen.this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vô hiệu hóa nút để tránh click nhiều lần
            loginButton.setEnabled(false);

            // *** Sử dụng AsyncTask để kiểm tra đăng nhập với Room ***
            new LoginUserTask(this).execute(inputUser, inputPass);

        });
    }

    // AsyncTask để kiểm tra đăng nhập trong background
    private static class LoginUserTask extends AsyncTask<String, Void, User> {
        private WeakReference<Main_Screen> activityReference;
        private String inputUsername; // Lưu lại để sử dụng trong onPostExecute nếu cần
        private String inputPassword; // Lưu lại để xác thực

        LoginUserTask(Main_Screen context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected User doInBackground(String... params) {
            Main_Screen activity = activityReference.get();
            // Kiểm tra activity còn tồn tại không
            if (activity == null || activity.isFinishing()) {
                Log.w(TAG, "Activity is finishing or null in doInBackground");
                return null;
            }

            inputUsername = params[0];
            inputPassword = params[1];

            Log.d(TAG, "Bắt đầu kiểm tra đăng nhập cho: " + inputUsername);

            // 1. Lấy UserDAO
            UserDatabase db = UserDatabase.getInstance(activity.getApplicationContext());
            UserDAO userDao = db.userDao();

            // 2. Tìm user bằng username (Thao tác DB)
            User user = userDao.findByUsername(inputUsername);

            // 3. Nếu tìm thấy user, xác thực mật khẩu
            if (user != null) {
                Log.d(TAG, "Tìm thấy user: " + user.getUsername());
                String storedHashedPassword = user.getHashedPassword();
                // Gọi PasswordHasher để xác thực (sử dụng PasswordHasher đã sửa)
                if (PasswordHasher.verifyPassword(inputPassword, storedHashedPassword)) {
                    Log.i(TAG, "Mật khẩu khớp cho user: " + inputUsername);
                    return user; // Mật khẩu khớp, trả về user object
                } else {
                    Log.w(TAG, "Mật khẩu KHÔNG khớp cho user: " + inputUsername);
                    return null; // Mật khẩu không khớp
                }
            } else {
                // Không tìm thấy user
                Log.w(TAG, "Không tìm thấy user: " + inputUsername);
                return null; // User không tồn tại
            }
        }

        @Override
        protected void onPostExecute(User loggedInUser) {
            Main_Screen activity = activityReference.get();
            // Kiểm tra activity còn tồn tại không
            if (activity == null || activity.isFinishing()) {
                Log.w(TAG, "Activity is finishing or null in onPostExecute");
                return; // Không làm gì nếu activity đã bị hủy
            }

            // Bật lại nút đăng nhập
            activity.loginButton.setEnabled(true);

            if (loggedInUser != null) {
                // --- Đăng nhập thành công ---
                Log.i(TAG, "Đăng nhập thành công với username: " + loggedInUser.getUsername());

                // Lưu username vào Default SharedPreferences để duy trì trạng thái
                SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                SharedPreferences.Editor editor = defaultPrefs.edit();
                // Sử dụng username làm định danh (đảm bảo username là unique)
                Log.i(TAG, "Lưu vào DefaultSharedPreferences - Key: '" + KEY_CURRENT_USER_ID + "', Value: '" + loggedInUser.getUsername() + "'");
                editor.putString(KEY_CURRENT_USER_ID, loggedInUser.getUsername());
                editor.apply(); // Sử dụng apply() cho hiệu năng tốt hơn trên main thread

                // Kiểm tra lại (tùy chọn)
                String checkUserId = defaultPrefs.getString(KEY_CURRENT_USER_ID, null);
                if (checkUserId == null || !checkUserId.equals(loggedInUser.getUsername())) {
                    Log.e(TAG, "Lỗi khi lưu/đọc trạng thái đăng nhập từ SharedPreferences!");
                    Toast.makeText(activity, "Lỗi lưu trạng thái đăng nhập!", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(activity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // Chuyển sang MainActivity
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
                activity.finish(); // Đóng màn hình đăng nhập

            } else {
                // --- Đăng nhập thất bại ---
                Log.w(TAG, "Đăng nhập thất bại cho username: " + inputUsername);
                Toast.makeText(activity, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                // Xóa trường password để người dùng nhập lại
                activity.passwordInput.setText("");
                activity.usernameInput.requestFocus(); // Focus lại ô username
            }
        }
    }
}