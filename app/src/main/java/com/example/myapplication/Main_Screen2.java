package com.example.myapplication; // Đảm bảo package đúng

import android.content.Intent;
// import android.content.SharedPreferences; // <-- Không cần SharedPreferences nữa
import android.os.AsyncTask; // *** THÊM AsyncTask ***
import android.os.Bundle;
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

public class Main_Screen2 extends AppCompatActivity {

    private EditText edtName, edtUser, edtPhone, edtPassword, edtAddress;
    private Button btnRegister;
    private static final String TAG = "MainScreen2_Register_Room"; // Đổi Tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen2);

        // --- Điều chỉnh padding cho Edge-to-Edge ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Ánh xạ các thành phần giao diện ---
        edtName = findViewById(R.id.edtName);
        edtUser = findViewById(R.id.edtUser);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtAddress = findViewById(R.id.edtAddress); // Đã có sẵn
        btnRegister = findViewById(R.id.btnRegister);

        // Xóa bỏ phần khởi tạo SharedPreferences "last_user_index"

        // --- Xử lý sự kiện click nút đăng ký (SỬ DỤNG ROOM VÀ ASYNCTASK) ---
        btnRegister.setOnClickListener(v -> attemptRegisterUser());
    }

    private void attemptRegisterUser() {
        String name = edtName.getText().toString().trim();
        String username = edtUser.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString(); // Lấy mật khẩu gốc, không trim
        String address = edtAddress.getText().toString().trim();

        // Kiểm tra các trường bắt buộc (ví dụ: Tên, Username, Password)
        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên, Tên đăng nhập và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Có thể thêm các validation khác (độ dài mật khẩu, định dạng username, v.v.)

        // Vô hiệu hóa nút để tránh click nhiều lần
        btnRegister.setEnabled(false);

        // Tạo đối tượng User tạm thời để truyền vào AsyncTask
        // Mật khẩu sẽ được băm trong AsyncTask
        // Đặt giá trị null hoặc rỗng cho các trường tùy chọn nếu chúng rỗng
        String phoneToSave = phone.isEmpty() ? null : phone;
        String addressToSave = address.isEmpty() ? null : address;

        // *** Sử dụng AsyncTask để thực hiện đăng ký với Room ***
        // Truyền các thông tin cần thiết, mật khẩu gốc sẽ được băm bên trong
        new RegisterUserTask(this).execute(username, name, phoneToSave, password, addressToSave);
    }

    // AsyncTask để đăng ký người dùng trong background
    private static class RegisterUserTask extends AsyncTask<String, Void, Long> {
        private WeakReference<Main_Screen2> activityReference;
        private String inputUsername; // Lưu lại username để hiển thị log/thông báo

        RegisterUserTask(Main_Screen2 context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Long doInBackground(String... params) {
            Main_Screen2 activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.w(TAG, "Activity is finishing or null in doInBackground");
                return -2L; // Mã lỗi khác -1L để phân biệt lỗi activity và lỗi DB
            }

            inputUsername = params[0];
            String name = params[1];
            String phone = params[2]; // Đã xử lý null ở trên
            String rawPassword = params[3]; // Mật khẩu gốc
            String address = params[4]; // Đã xử lý null ở trên

            Log.d(TAG, "Bắt đầu đăng ký cho user: " + inputUsername);

            // 1. Băm mật khẩu (Sử dụng PasswordHasher đã sửa)
            byte[] salt = PasswordHasher.generateSalt();
            String hashedPassword = PasswordHasher.hashPassword(rawPassword, salt);

            if (hashedPassword == null) {
                Log.e(TAG, "Lỗi băm mật khẩu cho user: " + inputUsername);
                return -3L; // Mã lỗi riêng cho lỗi băm
            }
            Log.d(TAG, "Mật khẩu đã được băm thành công.");

            // 2. Tạo đối tượng User với mật khẩu đã băm
            User newUser = new User(inputUsername, name, phone, hashedPassword, address);

            // 3. Lấy UserDAO và thực hiện insert (Thao tác DB)
            UserDatabase db = UserDatabase.getInstance(activity.getApplicationContext());
            try {
                // insert trả về rowId (> 0 nếu thành công)
                // hoặc -1L nếu conflict (username đã tồn tại do OnConflictStrategy.IGNORE)
                long result = db.userDao().insert(newUser);
                Log.d(TAG, "Kết quả insert user '" + inputUsername + "': " + result);
                return result;
            } catch (Exception e) {
                Log.e(TAG, "Lỗi Exception khi insert user vào Room", e);
                return -4L; // Mã lỗi riêng cho lỗi DB exception
            }
        }

        @Override
        protected void onPostExecute(Long result) {
            Main_Screen2 activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.w(TAG, "Activity is finishing or null in onPostExecute");
                return; // Không làm gì nếu activity đã bị hủy
            }

            // Bật lại nút đăng ký
            activity.btnRegister.setEnabled(true);

            if (result > 0) { // Insert thành công (rowId > 0)
                Log.i(TAG, "Đăng ký thành công cho user: " + inputUsername + " (ID: " + result + ")");
                Toast.makeText(activity, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                // Chuyển đến trang đăng nhập
                Intent intent = new Intent(activity, Main_Screen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Xóa stack cũ, về màn hình Login mới
                activity.startActivity(intent);
                activity.finish(); // Đóng màn hình đăng ký
            } else if (result == -1L) { // Bị IGNORE do username đã tồn tại
                Log.w(TAG, "Đăng ký thất bại cho user: " + inputUsername + ". Username đã tồn tại.");
                Toast.makeText(activity, "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.", Toast.LENGTH_LONG).show();
                activity.edtUser.setError("Tên đăng nhập đã tồn tại"); // Đánh dấu lỗi
                activity.edtUser.requestFocus();
            } else { // Các mã lỗi khác (-2L, -3L, -4L) hoặc lỗi không mong đợi
                Log.e(TAG, "Đăng ký thất bại cho user: " + inputUsername + ". Mã lỗi: " + result);
                Toast.makeText(activity, "Đã xảy ra lỗi trong quá trình đăng ký.", Toast.LENGTH_LONG).show();
            }
        }
    }
}