package com.example.myapplication.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log; // Thêm Log để debug nếu cần

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.Benhan_option;
import com.example.myapplication.Camera2;
import com.example.myapplication.R; // Đảm bảo R được import
import com.example.myapplication.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment"; // Thêm TAG để log
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // --- Xử lý nút mở Camera2 ---
        // Không cần kiểm tra null nếu ID btn3 chắc chắn tồn tại trong XML
        binding.btn3.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(requireContext(), Camera2.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Không thể khởi chạy Camera2 Activity", e);
                // Có thể hiển thị Toast cho người dùng ở đây
            }
        });


        // --- Xử lý nút chuyển đến Benhan_option ---
        // Đảm bảo ID btnGoToBenhanOption tồn tại chính xác trong fragment_home.xml
        // và project đã được Rebuild/Sync
        try {
            // Không cần kiểm tra null nếu ID chắc chắn tồn tại trong XML
            binding.btn1.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(requireContext(), Benhan_option.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Không thể khởi chạy Benhan_option Activity", e);
                    // Có thể hiển thị Toast cho người dùng ở đây
                }
            });
        } catch (NullPointerException e) {
            // Lỗi này xảy ra nếu binding.btnGoToBenhanOption là null
            // (thường là do sai ID trong XML hoặc chưa rebuild)
            Log.e(TAG, "LỖI: Không tìm thấy Button với ID 'btnGoToBenhanOption'. Kiểm tra lại file fragment_home.xml và Rebuild Project.", e);
            // Thông báo cho người dùng hoặc xử lý phù hợp
        }


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}