package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Benhan_option extends AppCompatActivity {

    private Button btnView, btnEdit, btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_benhan_option);

        btnView = findViewById(R.id.check1);
        btnEdit = findViewById(R.id.check2);
        btnAdd = findViewById(R.id.check3);

        // Nút Xem: Mở danh sách bệnh án
        if (btnView != null) {
            btnView.setOnClickListener(view -> {
                Intent intent = new Intent(Benhan_option.this, ViewBenhAnActivity.class);
                startActivity(intent);
            });
        }

        // Nút Sửa: Cũng mở danh sách bệnh án (để chọn bệnh án cần sửa)
        if (btnEdit != null) {
            btnEdit.setOnClickListener(view -> {
                Intent intent = new Intent(Benhan_option.this, ViewBenhAnActivity.class);
                startActivity(intent);
                // Có thể thêm Toast để giải thích luồng:
                // Toast.makeText(Benhan_option.this, "Chọn bệnh án từ danh sách để sửa", Toast.LENGTH_SHORT).show();
            });
        }

        // Nút Thêm: Mở màn hình thêm mới
        if (btnAdd != null) {
            btnAdd.setOnClickListener(view -> {
                Intent intent = new Intent(Benhan_option.this, AddBenhAnActivity.class);
                startActivity(intent);
            });
        }
    }
}