package com.example.myapplication; // Đảm bảo package đúng

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity_Room";

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView nameTextView;
    private TextView usernameTextView;
    private DrawerLayout drawer;
    private int currentLoggedInUserId = -1; // Lưu User ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);
        // binding.appBarMain.fab.setVisibility(View.GONE); // Để lại nếu bạn không dùng FloatingActionButton

        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);
        // Đảm bảo các ID này khớp với nav_header_main.xml
        nameTextView = headerView.findViewById(R.id.textName);
        usernameTextView = headerView.findViewById(R.id.textUsername);

        // <<< THAY ĐỔI CHÍNH: Lấy User ID từ SharedPreferences >>>
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentLoggedInUserId = defaultPrefs.getInt(Main_Screen.KEY_LOGGED_IN_USER_ID_INT, -1);

        // --- THÊM DÒNG LOG NÀY ĐỂ XÁC NHẬN GIÁ TRỊ ID ---
        Log.d(TAG, "onCreate: Giá trị User ID từ SharedPreferences: " + currentLoggedInUserId);
        // -----------------------------------------------


        // --- THÊM KIỂM TRA NGAY LÚC START ---
        if (currentLoggedInUserId == -1) {
            Log.w(TAG, "onCreate: Không tìm thấy User ID trong SharedPreferences khi khởi động. Chuyển về màn hình đăng nhập.");
            Toast.makeText(this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            logout(); // Tự động đăng xuất nếu không có ID hợp lệ
            return; // Thoát khỏi onCreate
        }
        // ------------------------------------

        loadUserInfoForDrawer(); // Gọi hàm load thông tin nếu có ID hợp lệ

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
        ).setOpenableLayout(drawer).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadUserInfoForDrawer() {
        // Kiểm tra null cho TextViews ở đây không cần thiết lắm vì nó đã được ánh xạ ở onCreate
        // và chúng ta sẽ kiểm tra lại trong onPostExecute.
        Log.d(TAG, "loadUserInfoForDrawer: Đang tải thông tin cho user ID: " + currentLoggedInUserId);
        new LoadUserInfoByIdTask(this).execute(currentLoggedInUserId);
    }

    private static class LoadUserInfoByIdTask extends AsyncTask<Integer, Void, User> {
        private WeakReference<MainActivity> activityReference;
        private int userIdToLoad;

        LoadUserInfoByIdTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected User doInBackground(Integer... params) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.w(TAG, "LoadUserInfoByIdTask: Activity is finishing or null in doInBackground");
                return null;
            }

            userIdToLoad = params[0];
            Log.d(TAG, "LoadUserInfoByIdTask: Bắt đầu lấy thông tin cho user ID: " + userIdToLoad);
            AppDatabase db = AppDatabase.getInstance(activity.getApplicationContext());
            try {
                return db.userDao().findById(userIdToLoad);
            } catch (Exception e) {
                Log.e(TAG, "LoadUserInfoByIdTask: Lỗi khi lấy User ID '" + userIdToLoad + "' từ Room", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.w(TAG, "LoadUserInfoByIdTask: Activity is finishing or null in onPostExecute");
                return;
            }

            if (user != null) {
                Log.d(TAG, "LoadUserInfoByIdTask: User object found. Name: '" + user.getName() + "', Username: '" + user.getUsername() + "'");

                if (activity.nameTextView != null && activity.usernameTextView != null) {
                    Log.d(TAG, "LoadUserInfoByIdTask: TextViews found, setting text...");
                    activity.nameTextView.setText(user.getName());
                    activity.usernameTextView.setText(user.getUsername());
                    Log.i(TAG, "LoadUserInfoByIdTask: Cập nhật thông tin user thành công trên TextViews.");
                } else {
                    Log.e(TAG, "LoadUserInfoByIdTask: nameTextView hoặc usernameTextView là null trong onPostExecute!");
                }

            } else {
                Log.w(TAG, "LoadUserInfoByIdTask: Không tìm thấy thông tin cho user ID " + userIdToLoad + " trong database. Phiên đăng nhập không hợp lệ?");
                Toast.makeText(activity, "Lỗi tải thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                activity.logout();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        if (id == R.id.nav_logout) {
            logout();
            // Không cần close drawer ở đây vì Activity sẽ finish và chuyển sang Login
            return true;
        }
        // Thêm xử lý cho các mục menu Bệnh án và Camera2 nếu có trong XML
        if (id == R.id.nav_benhan_option) {
            Intent intent = new Intent(this, Benhan_option.class);
            startActivity(intent);
            drawer.closeDrawers();
            return true;
        } else if (id == R.id.nav_camera2) {
            Intent intent = new Intent(this, Camera2.class);
            startActivity(intent);
            drawer.closeDrawers();
            return true;
        }
        // --------------------------------------------------------------

        boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
        if (handled && drawer.isOpen()) {
            drawer.closeDrawers();
        }
        return handled;
    }

    private void logout() {
        Log.i(TAG, "Thực hiện đăng xuất...");
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = defaultPrefs.edit();
        editor.remove(Main_Screen.KEY_LOGGED_IN_USER_ID_INT);
        editor.apply();

        // (Tùy chọn) AppDatabase.closeInstance();

        Intent intent = new Intent(this, Main_Screen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}