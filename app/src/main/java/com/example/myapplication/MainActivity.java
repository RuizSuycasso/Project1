package com.example.myapplication; // Đảm bảo package đúng

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask; // *** THÊM AsyncTask ***
import android.os.Bundle;
import android.preference.PreferenceManager; // *** THÊM PreferenceManager ***
import android.util.Log; // *** THÊM Log ***
import android.view.MenuItem;
import android.view.View;
import android.widget.Button; // Giữ lại nếu nút btn1 còn sử dụng
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
// import com.google.android.material.snackbar.Snackbar; // Giữ lại nếu FAB còn sử dụng

import java.lang.ref.WeakReference; // *** THÊM WeakReference ***

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity_Room"; // Đổi Tag

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView nameTextView; // TextView cho tên đầy đủ
    private TextView usernameTextView; // TextView cho username
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // Xử lý FAB nếu cần (đã có sẵn)
        /*
        binding.appBarMain.fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setAnchorView(R.id.fab).show());
        */
        // Ẩn FAB nếu không dùng
        binding.appBarMain.fab.setVisibility(View.GONE);


        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Lấy headerView và ánh xạ TextViews
        View headerView = navigationView.getHeaderView(0);
        // *** Đảm bảo ID trong nav_header_main.xml là chính xác ***
        nameTextView = headerView.findViewById(R.id.textName);
        usernameTextView = headerView.findViewById(R.id.textUsername);

        // --- Tải thông tin User cho Drawer Header (Sử dụng Room và AsyncTask) ---
        loadUserInfoForDrawer();

        // --- Cấu hình Navigation Component ---
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                // Thêm các ID Fragment cấp cao nhất của bạn vào đây
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
                // , R.id.your_other_top_level_fragment_id
        )
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this); // Đặt Listener

        // Xóa bỏ addOnDestinationChangedListener nếu không cần thiết nữa hoặc đã được xử lý bởi NavigationUI

        // Xử lý sự kiện cho Button btn1 (giữ lại nếu cần)
        // Cần đảm bảo R.id.btn1 tồn tại trong layout activity_main.xml (không phải content_main.xml)
        /*
        Button button3 = findViewById(R.id.btn1); // Tìm trong layout của MainActivity
         if(button3 != null) { // Kiểm tra null trước khi đặt listener
            button3.setOnClickListener(p -> {
                Intent intent = new Intent(MainActivity.this, Benhan_option.class); // Đảm bảo Benhan_option tồn tại
                startActivity(intent);
            });
         } else {
             Log.w(TAG, "Button với ID 'btn1' không tìm thấy trong layout MainActivity.");
         }
         */
    }

    // --- Hàm tải thông tin User từ Room ---
    private void loadUserInfoForDrawer() {
        // Đọc username đã lưu từ DefaultSharedPreferences khi đăng nhập thành công
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currentUsername = defaultPrefs.getString(Main_Screen.KEY_CURRENT_USER_ID, null);

        if (usernameTextView == null || nameTextView == null) {
            Log.e(TAG, "TextViews trong nav header chưa được ánh xạ!");
            return;
        }

        if (currentUsername != null) {
            Log.d(TAG, "Đang tải thông tin cho user: " + currentUsername);
            // Hiển thị username ngay lập tức (vì đã có sẵn)
            usernameTextView.setText(currentUsername);
            // Lấy tên đầy đủ từ DB ở background
            new LoadUserNameTask(this).execute(currentUsername);
        } else {
            Log.w(TAG, "Không tìm thấy " + Main_Screen.KEY_CURRENT_USER_ID + " trong SharedPreferences. Đăng xuất?");
            // Có thể xử lý như logout tự động hoặc hiển thị giá trị mặc định/lỗi
            nameTextView.setText("Lỗi tải tên");
            usernameTextView.setText("Lỗi tải user");
            // Cân nhắc gọi logout() ở đây nếu muốn bắt buộc đăng nhập lại
            logout(); // Ví dụ: bắt buộc đăng xuất nếu không có ID
        }
    }

    // AsyncTask để lấy tên đầy đủ từ DB
    private static class LoadUserNameTask extends AsyncTask<String, Void, String> {
        private WeakReference<MainActivity> activityReference;

        LoadUserNameTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... params) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return null;

            String username = params[0];
            Log.d(TAG, "LoadUserNameTask: Bắt đầu lấy tên cho user: " + username);
            UserDatabase db = UserDatabase.getInstance(activity.getApplicationContext());
            try {
                // Sử dụng query chỉ lấy tên để tiết kiệm tài nguyên
                String name = db.userDao().getNameByUsername(username);
                Log.d(TAG, "LoadUserNameTask: Tên lấy từ DB: " + name);
                return name;
            } catch (Exception e) {
                Log.e(TAG, "LoadUserNameTask: Lỗi khi lấy tên user '" + username + "' từ Room", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String name) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing() || activity.nameTextView == null) return;

            if (name != null) {
                // Cập nhật TextView tên đầy đủ
                activity.nameTextView.setText(name);
                Log.i(TAG, "LoadUserNameTask: Cập nhật tên thành công: " + name);
            } else {
                // Xử lý trường hợp không tìm thấy tên (user bị xóa?)
                Log.w(TAG, "LoadUserNameTask: Không tìm thấy tên cho user.");
                activity.nameTextView.setText("Không tìm thấy tên"); // Hoặc giá trị mặc định khác
            }
        }
    }
    // --- Kết thúc phần tải thông tin user ---


    // --- Xử lý lựa chọn item trong Navigation Drawer ---
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        if (id == R.id.nav_logout) { // ID của item logout trong menu drawer
            logout();
            drawer.close(); // Đóng drawer sau khi chọn logout
            return true; // Đã xử lý
        }
        // Thêm các xử lý else if cho các item chuyển Activity khác nếu cần
        /*
         else if (id == R.id.nav_transactions) {
             startActivity(new Intent(this, TransactionActivity.class));
             drawer.close();
             return true;
         }
        */

        // Để NavigationUI xử lý các điều hướng Fragment còn lại
        boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

        // Đóng drawer sau khi chọn một item (nếu chưa đóng)
        if (handled && drawer.isOpen()) {
            drawer.close();
        }

        return handled; // Trả về true nếu NavigationUI đã xử lý
    }

    // --- Hàm Logout ---
    private void logout() {
        Log.i(TAG, "Thực hiện đăng xuất...");

        // 1. Xóa trạng thái đăng nhập khỏi DefaultSharedPreferences
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = defaultPrefs.edit();
        Log.d(TAG, "Đang xóa key '" + Main_Screen.KEY_CURRENT_USER_ID + "' khỏi DefaultSharedPreferences.");
        editor.remove(Main_Screen.KEY_CURRENT_USER_ID);
        boolean success = editor.commit(); // Dùng commit ở đây có thể chấp nhận được
        Log.d(TAG, "Xóa key đăng nhập thành công: " + success);

        // 2. (Tùy chọn) Đóng Database Instance
        // UserDatabase.closeInstance();
        // Log.d(TAG, "Đã đóng UserDatabase instance (tùy chọn).");

        // 3. Chuyển về màn hình đăng nhập và xóa stack Activity cũ
        Log.i(TAG, "Chuyển về Main_Screen và xóa Activity stack.");
        Intent intent = new Intent(this, Main_Screen.class);
        // FLAG_ACTIVITY_NEW_TASK: Bắt đầu activity này trong một task mới.
        // FLAG_ACTIVITY_CLEAR_TASK: Xóa task hiện tại trước khi bắt đầu task mới.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // finishAffinity(); // Hoặc dùng finishAffinity() để đóng tất cả activity trong task hiện tại
        finish(); // Đóng MainActivity hiện tại

        // 4. Thông báo cho người dùng
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    // --- Hỗ trợ nút Up/Back trên ActionBar ---
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}