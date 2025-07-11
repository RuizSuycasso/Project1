// File: src/main/java/com/example/myapplication/DrugInfo.java
// Phiên bản cuối cùng, thêm annotation của Gson

package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName; // <<< THÊM IMPORT NÀY

/**
 * Model chứa thông tin thuốc, đã được tối ưu cho Gson.
 */
public class DrugInfo implements Parcelable {

    @SerializedName("drug") // <<< THÊM ANNOTATION NÀY
    private String drug;

    @SerializedName("dosage") // <<< THÊM ANNOTATION NÀY
    private String dosage;

    // Constructor để tạo đối tượng (hữu ích cho việc test)
    public DrugInfo(String drug, String dosage) {
        this.drug = drug;
        this.dosage = dosage;
    }

    // Constructor rỗng mà Gson có thể cần
    public DrugInfo() {}

    // Các hàm getter để lấy thông tin
    public String getDrug() {
        return drug;
    }



    public String getDosage() {
        return dosage;
    }

    // --- Parcelable Implementation (giữ nguyên) ---
    protected DrugInfo(Parcel in) {
        drug = in.readString();
        dosage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(drug);
        dest.writeString(dosage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DrugInfo> CREATOR = new Creator<DrugInfo>() {
        @Override
        public DrugInfo createFromParcel(Parcel in) {
            return new DrugInfo(in);
        }

        @Override
        public DrugInfo[] newArray(int size) {
            return new DrugInfo[size];
        }
    };
}