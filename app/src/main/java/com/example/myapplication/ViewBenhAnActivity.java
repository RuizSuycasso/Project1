package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
// AppDatabase đã được import ngầm

public class ViewBenhAnActivity extends AppCompatActivity {

    private static final String TAG = "ViewBenhAnActivity";

    private RecyclerView recyclerView;
    private BenhAnAdapter adapter;
    private TextView tvEmptyList;
    private BenhAnDao benhAnDao;
    private int currentLoggedInUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_benh_an);

        recyclerView = findViewById(R.id.recyclerViewBenhAn);
        tvEmptyList = findViewById(R.id.tvEmptyList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Sử dụng constructor mặc định
        adapter = new BenhAnAdapter();
        recyclerView.setAdapter(adapter);

        benhAnDao = AppDatabase.getInstance(this).benhAnDao();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        currentLoggedInUserId = prefs.getInt(Main_Screen.KEY_LOGGED_IN_USER_ID_INT, -1);

        if (currentLoggedInUserId == -1) {
            Log.e(TAG, "Không lấy được ID người dùng từ SharedPreferences. Chuyển về màn hình đăng nhập.");
            Toast.makeText(this, "Lỗi phiên đăng nhập, vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            tvEmptyList.setText("Lỗi: Không xác định được người dùng.");
            tvEmptyList.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            // <<< Tự động chuyển về màn hình đăng nhập nếu không có User ID hợp lệ >>>
            // Nếu bạn muốn ứng dụng tự động logout và chuyển màn hình, bạn có thể thêm code ở đây
            // Ví dụ: startActivity(new Intent(this, Main_Screen.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            // finish();
            // ----------------------------------------------------------------------
            return; // Thoát khỏi onCreate nếu không có User ID
        } else {
            Log.i(TAG, "Đã lấy được User ID: " + currentLoggedInUserId + " để hiển thị bệnh án.");
        }

        adapter.setOnItemClickListener(benhAn -> {
            Intent intent = new Intent(ViewBenhAnActivity.this, EditBenhAnActivity.class);
            // <<< ĐÃ SỬA LỖI Ở ĐÂY: Sử dụng hằng số EXTRA_BENHAN_ID từ EditBenhAnActivity >>>
            intent.putExtra(EditBenhAnActivity.EXTRA_BENHAN_ID, benhAn.id);
            // -------------------------------------------------------------------------
            startActivity(intent);
        });

        setTitle("Danh sách Bệnh án của bạn");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Chỉ tải dữ liệu nếu có User ID hợp lệ
        if (currentLoggedInUserId != -1) {
            loadBenhAnDataForCurrentUser();
        } else {
            // Nếu đến onResume mà User ID không hợp lệ (điều này khó xảy ra với kiểm tra ở onCreate, nhưng để an toàn)
            Log.w(TAG, "onResume: User ID không hợp lệ. Không tải dữ liệu bệnh án.");
            tvEmptyList.setText("Lỗi: Không xác định được người dùng.");
            tvEmptyList.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void loadBenhAnDataForCurrentUser() {
        Log.d(TAG, "Bắt đầu tải bệnh án cho User ID: " + currentLoggedInUserId);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Lấy danh sách bệnh án chỉ thuộc về người dùng hiện tại
            List<BenhAn> userSpecificList = benhAnDao.getBenhAnByUserId(currentLoggedInUserId);

            runOnUiThread(() -> {
                if (userSpecificList != null) {
                    Log.d(TAG, "Số lượng bệnh án tìm thấy cho User ID " + currentLoggedInUserId + ": " + userSpecificList.size());
                    adapter.setBenhAnList(userSpecificList); // Phương thức này sẽ xử lý list rỗng hoặc null

                    if (userSpecificList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        tvEmptyList.setText("Bạn chưa có bệnh án nào.");
                        tvEmptyList.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        tvEmptyList.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "Danh sách bệnh án trả về là null cho User ID: " + currentLoggedInUserId);
                    // Trường hợp này rất hiếm với Room, nhưng vẫn xử lý
                    adapter.setBenhAnList(null); // Để adapter xử lý thành list rỗng
                    recyclerView.setVisibility(View.GONE);
                    tvEmptyList.setText("Lỗi khi tải danh sách bệnh án.");
                    tvEmptyList.setVisibility(View.VISIBLE);
                }
            });
        });
    }
}