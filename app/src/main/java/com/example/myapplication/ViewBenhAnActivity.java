package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast; // Import Toast

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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
        tvEmptyList = findViewById(R.id.tvEmptyList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Tối ưu hóa nếu kích thước item không đổi

        adapter = new BenhAnAdapter();
        recyclerView.setAdapter(adapter);

        // Lấy DAO
        benhAnDao = AppDatabase.getInstance(this).benhAnDao();

        // --- CẢNH BÁO: Thao tác DB trên luồng chính ---
        loadBenhAnData();
        // -------------------------------------------

        // Xử lý sự kiện click vào item
        adapter.setOnItemClickListener(benhAn -> {
            // Mở EditBenhAnActivity và truyền ID
            Intent intent = new Intent(ViewBenhAnActivity.this, EditBenhAnActivity.class);
            // Đặt tên EXTRA rõ ràng
            intent.putExtra(EditBenhAnActivity.EXTRA_BENHAN_ID, benhAn.id);
            startActivity(intent);
            // Không cần startActivityForResult nếu chỉ muốn mở và sửa
        });
    }

    // Load lại dữ liệu khi Activity quay lại (ví dụ sau khi sửa/xóa)
    @Override
    protected void onResume() {
        super.onResume();
        // --- CẢNH BÁO: Thao tác DB trên luồng chính ---
        loadBenhAnData();
        // -------------------------------------------
    }

    private void loadBenhAnData() {
        // --- CẢNH BÁO: Thao tác DB trên luồng chính ---
        List<BenhAn> list = benhAnDao.getAllBenhAn();
        // -------------------------------------------

        adapter.setBenhAnList(list); // Cập nhật dữ liệu cho adapter

        // Hiển thị thông báo nếu danh sách rỗng
        if (list == null || list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.GONE);
        }
    }
}