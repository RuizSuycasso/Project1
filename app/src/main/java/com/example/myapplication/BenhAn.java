package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.ForeignKey; // <<< THÊM
import androidx.room.Index;    // <<< THÊM
import androidx.room.PrimaryKey;

// <<< THAY ĐỔI CHÍNH >>>
@Entity(tableName = "benhan_table",
        // Khai báo khóa ngoại từ BenhAn.userId đến User.id
        foreignKeys = @ForeignKey(entity = User.class,      // Tham chiếu đến User Entity
                parentColumns = "id",     // Cột 'id' trong bảng 'users'
                childColumns = "userId",  // Cột 'userId' trong bảng 'benhan_table'
                onDelete = ForeignKey.CASCADE // Hành động khi User bị xóa (CASCADE: xóa luôn BenhAn liên quan)
                // Các lựa chọn khác: SET_NULL, RESTRICT, NO_ACTION
        ),
        indices = {@Index("userId")} // Tạo index trên cột userId để truy vấn nhanh hơn
)
public class BenhAn {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId; // <<<< THÊM TRƯỜNG NÀY ĐỂ LIÊN KẾT VỚI USER

    public String diagnosis;
    public String medicalHistory;
    public String labResults;
    public String allergies;
    public String currentMedications;
    public String diseaseStage;

    // Constructor mặc định cho Room
    public BenhAn() {}

    // Getter và Setter cho userId (quan trọng)
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}