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

    // --- DI CHUYỂN HÀM checkNullOrEmpty RA ĐÂY ---
    // Đây là phương thức trợ giúp của lớp BenhAnAdapter
    private String checkNullOrEmpty(String text) {
        return (text != null && !text.trim().isEmpty()) ? text : "N/A";
    }
    // -------------------------------------------

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

        // Gán dữ liệu cho tất cả các TextView
        holder.tvItemId.setText("ID: " + currentBenhAn.id);

        // Bây giờ gọi hàm checkNullOrEmpty (đã được định nghĩa bên ngoài)
        holder.tvItemDiagnosis.setText("Chẩn đoán: " + checkNullOrEmpty(currentBenhAn.diagnosis));
        holder.tvItemMedicalHistory.setText("Tiền sử: " + checkNullOrEmpty(currentBenhAn.medicalHistory));
        holder.tvItemLabResults.setText(checkNullOrEmpty(currentBenhAn.labResults));
        holder.tvItemAllergies.setText(checkNullOrEmpty(currentBenhAn.allergies));
        holder.tvItemCurrentMedications.setText(checkNullOrEmpty(currentBenhAn.currentMedications));
        holder.tvItemDiseaseStage.setText(checkNullOrEmpty(currentBenhAn.diseaseStage));
    }

    @Override
    public int getItemCount() {
        return benhAnList.size();
    }

    public void setBenhAnList(List<BenhAn> benhAnList) {
        this.benhAnList = benhAnList != null ? benhAnList : new ArrayList<>();
        notifyDataSetChanged();
    }

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
        private TextView tvItemLabResults;
        private TextView tvItemAllergies;
        private TextView tvItemCurrentMedications;
        private TextView tvItemDiseaseStage;

        public BenhAnViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemId = itemView.findViewById(R.id.tvItemId);
            tvItemDiagnosis = itemView.findViewById(R.id.tvItemDiagnosis);
            tvItemMedicalHistory = itemView.findViewById(R.id.tvItemMedicalHistory);
            tvItemLabResults = itemView.findViewById(R.id.tvItemLabResults);
            tvItemAllergies = itemView.findViewById(R.id.tvItemAllergies);
            tvItemCurrentMedications = itemView.findViewById(R.id.tvItemCurrentMedications);
            tvItemDiseaseStage = itemView.findViewById(R.id.tvItemDiseaseStage);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(benhAnList.get(position));
                }
            });
        }
    }

    // --- Interface và Listener (Giữ nguyên) ---
    public interface OnItemClickListener {
        void onItemClick(BenhAn benhAn);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}