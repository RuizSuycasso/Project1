package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
// Cần import AppDatabase
import com.example.myapplication.AppDatabase;

public class ViewBenhAnActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BenhAnAdapter adapter;
    private TextView tvEmptyList;
    private BenhAnDao benhAnDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_benh_an);

        recyclerView = findViewById(R.id.recyclerViewBenhAn);
        tvEmptyList = findViewById(R.id.tvEmptyList); // Đảm bảo ID này đúng trong XML

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new BenhAnAdapter();
        recyclerView.setAdapter(adapter);

        benhAnDao = AppDatabase.getInstance(this).benhAnDao();

        // Không load ở đây nữa, sẽ load trong onResume

        adapter.setOnItemClickListener(benhAn -> {
            Intent intent = new Intent(ViewBenhAnActivity.this, EditBenhAnActivity.class);
            intent.putExtra(EditBenhAnActivity.EXTRA_BENHAN_ID, benhAn.id);
            startActivity(intent);
        });

        // Đặt tiêu đề
        setTitle("Danh sách Bệnh án");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // --- THAY ĐỔI: Load dữ liệu trên background thread mỗi khi quay lại ---
        loadBenhAnData();
        // ---------------------------------------------------------------
    }

    private void loadBenhAnData() {
        // --- THAY ĐỔI: Thực thi trên background thread ---
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<BenhAn> list = benhAnDao.getAllBenhAn();

            // Quay lại Main thread để cập nhật Adapter và UI
            runOnUiThread(() -> {
                adapter.setBenhAnList(list); // Cập nhật dữ liệu cho adapter

                // Hiển thị thông báo nếu danh sách rỗng
                if (list == null || list.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmptyList.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmptyList.setVisibility(View.GONE);
                }
            });
        });
        // ---------------------------------------------
    }
}