package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class Main_Screen extends AppCompatActivity {

    private static final String TAG = "MainScreen_Login_Room";
    public static final String KEY_LOGGED_IN_USER_ID_INT = "logged_in_user_id_int";

    private EditText usernameInput; // Sẽ được gán bằng R.id.editTextUsername
    private EditText passwordInput; // Sẽ được gán bằng R.id.editTextPassword
    private Button loginButton;     // Sẽ được gán bằng R.id.buttonLogin (hoặc R.id.button)
    private Button registerButton;  // Sẽ được gán bằng R.id.buttonRegister (hoặc R.id.button3)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen);

        // <<< THAY ĐỔI CHÍNH Ở ĐÂY >>>
        usernameInput = findViewById(R.id.editTextUsername); // Sử dụng ID mới từ XML
        passwordInput = findViewById(R.id.editTextPassword); // Sử dụng ID mới từ XML
        loginButton = findViewById(R.id.buttonLogin);     // Sử dụng ID mới nếu bạn đã đổi (buttonLogin)
        // Hoặc giữ nguyên nếu bạn không đổi: loginButton = findViewById(R.id.button);
        registerButton = findViewById(R.id.buttonRegister);  // Sử dụng ID mới nếu bạn đã đổi (buttonRegister)
        // Hoặc giữ nguyên nếu bạn không đổi: registerButton = findViewById(R.id.button3);


        registerButton.setOnClickListener(p -> {
            Intent intent = new Intent(Main_Screen.this, Main_Screen2.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginButton.setOnClickListener(p -> {
            String inputUser = usernameInput.getText().toString().trim();
            String inputPass = passwordInput.getText().toString();

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                Toast.makeText(Main_Screen.this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            loginButton.setEnabled(false);
            new LoginUserTask(this).execute(inputUser, inputPass);
        });
    }

    // AsyncTask LoginUserTask giữ nguyên như đã sửa trước đó
    private static class LoginUserTask extends AsyncTask<String, Void, User> {
        private WeakReference<Main_Screen> activityReference;
        private String inputUsernameForLog;

        LoginUserTask(Main_Screen context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected User doInBackground(String... params) {
            Main_Screen activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.w(TAG, "Activity is finishing or null in doInBackground");
                return null;
            }

            String inputUsername = params[0];
            String inputPassword = params[1];
            inputUsernameForLog = inputUsername;

            AppDatabase db = AppDatabase.getInstance(activity.getApplicationContext());
            UserDAO userDao = db.userDao();
            User user = userDao.findByUsername(inputUsername);

            if (user != null) {
                String storedHashedPassword = user.getHashedPassword();
                if (PasswordHasher.verifyPassword(inputPassword, storedHashedPassword)) {
                    return user;
                } else {
                    Log.w(TAG, "Mật khẩu KHÔNG khớp cho user: " + inputUsername);
                    return null;
                }
            } else {
                Log.w(TAG, "Không tìm thấy user: " + inputUsername);
                return null;
            }
        }

        @Override
        protected void onPostExecute(User loggedInUser) {
            Main_Screen activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.loginButton.setEnabled(true);

            if (loggedInUser != null) {
                SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                SharedPreferences.Editor editor = defaultPrefs.edit();
                editor.putInt(KEY_LOGGED_IN_USER_ID_INT, loggedInUser.getId());
                editor.apply();

                Toast.makeText(activity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
                activity.finish();
            } else {
                Toast.makeText(activity, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                activity.passwordInput.setText("");
                activity.usernameInput.requestFocus();
            }
        }
    }
}