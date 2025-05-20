package com.example.myapplication; // Đảm bảo package đúng

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.ref.WeakReference;

public class Main_Screen2 extends AppCompatActivity {

    private EditText edtName, edtUser, edtPhone, edtPassword, edtAddress;
    private Button btnRegister;
    private static final String TAG = "MainScreen2_Register_Room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtName = findViewById(R.id.edtName);
        edtUser = findViewById(R.id.edtUser);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtAddress = findViewById(R.id.edtAddress);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> attemptRegisterUser());
    }

    private void attemptRegisterUser() {
        String name = edtName.getText().toString().trim();
        String username = edtUser.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên, Tên đăng nhập và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        String phoneToSave = phone.isEmpty() ? null : phone;
        String addressToSave = address.isEmpty() ? null : address;

        new RegisterUserTask(this).execute(username, name, phoneToSave, password, addressToSave);
    }

    private static class RegisterUserTask extends AsyncTask<String, Void, Long> {
        private WeakReference<Main_Screen2> activityReference;
        private String inputUsername;

        RegisterUserTask(Main_Screen2 context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Long doInBackground(String... params) {
            Main_Screen2 activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.w(TAG, "Activity is finishing or null in doInBackground");
                return -2L;
            }

            inputUsername = params[0];
            String name = params[1];
            String phone = params[2];
            String rawPassword = params[3];
            String address = params[4];

            Log.d(TAG, "Bắt đầu đăng ký cho user: " + inputUsername);

            byte[] salt = PasswordHasher.generateSalt(); // Giả sử PasswordHasher.java tồn tại và hoạt động
            String hashedPassword = PasswordHasher.hashPassword(rawPassword, salt);

            if (hashedPassword == null) {
                Log.e(TAG, "Lỗi băm mật khẩu cho user: " + inputUsername);
                return -3L;
            }

            User newUser = new User(inputUsername, name, phone, hashedPassword, address);

            // <<< THAY ĐỔI CHÍNH: Sử dụng AppDatabase >>>
            AppDatabase db = AppDatabase.getInstance(activity.getApplicationContext());
            try {
                long result = db.userDao().insert(newUser);
                Log.d(TAG, "Kết quả insert user '" + inputUsername + "': " + result);
                return result;
            } catch (Exception e) {
                Log.e(TAG, "Lỗi Exception khi insert user vào Room", e);
                return -4L;
            }
        }

        @Override
        protected void onPostExecute(Long result) {
            Main_Screen2 activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.w(TAG, "Activity is finishing or null in onPostExecute");
                return;
            }
            activity.btnRegister.setEnabled(true);

            if (result > 0) {
                Log.i(TAG, "Đăng ký thành công cho user: " + inputUsername + " (ID: " + result + ")");
                Toast.makeText(activity, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, Main_Screen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                activity.finish();
            } else if (result == -1L) {
                Log.w(TAG, "Đăng ký thất bại cho user: " + inputUsername + ". Username đã tồn tại.");
                Toast.makeText(activity, "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.", Toast.LENGTH_LONG).show();
                activity.edtUser.setError("Tên đăng nhập đã tồn tại");
                activity.edtUser.requestFocus();
            } else {
                Log.e(TAG, "Đăng ký thất bại cho user: " + inputUsername + ". Mã lỗi: " + result);
                Toast.makeText(activity, "Đã xảy ra lỗi trong quá trình đăng ký.", Toast.LENGTH_LONG).show();
            }
        }
    }
}