package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BenhAnAdapter extends RecyclerView.Adapter<BenhAnAdapter.BenhAnViewHolder> {

    private List<BenhAn> benhAnList = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public BenhAnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_benhan, parent, false);
        return new BenhAnViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BenhAnViewHolder holder, int position) {
        BenhAn currentBenhAn = benhAnList.get(position);
        holder.tvItemId.setText("ID: " + currentBenhAn.id);
        holder.tvItemDiagnosis.setText("Chẩn đoán: " + (currentBenhAn.diagnosis != null ? currentBenhAn.diagnosis : "N/A"));
        holder.tvItemMedicalHistory.setText("Tiền sử: " + (currentBenhAn.medicalHistory != null ? currentBenhAn.medicalHistory : "N/A"));
        // Bind thêm dữ liệu nếu cần
    }

    @Override
    public int getItemCount() {
        return benhAnList.size();
    }

    // Phương thức để cập nhật dữ liệu cho Adapter
    public void setBenhAnList(List<BenhAn> benhAnList) {
        this.benhAnList = benhAnList != null ? benhAnList : new ArrayList<>();
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật lại giao diện
    }

    // Lấy bệnh án tại vị trí cụ thể (cần cho việc lấy ID khi click)
    public BenhAn getBenhAnAt(int position) {
        if (position >= 0 && position < benhAnList.size()) {
            return benhAnList.get(position);
        }
        return null;
    }

    // --- ViewHolder ---
    class BenhAnViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItemId;
        private TextView tvItemDiagnosis;
        private TextView tvItemMedicalHistory;

        public BenhAnViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemId = itemView.findViewById(R.id.tvItemId);
            tvItemDiagnosis = itemView.findViewById(R.id.tvItemDiagnosis);
            tvItemMedicalHistory = itemView.findViewById(R.id.tvItemMedicalHistory);

            // Bắt sự kiện click trên toàn bộ item view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(benhAnList.get(position));
                }
            });
        }
    }

    // --- Interface cho sự kiện Click ---
    public interface OnItemClickListener {
        void onItemClick(BenhAn benhAn);
    }

    // Phương thức để Activity/Fragment set listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}