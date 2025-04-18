package com.example.myapplication; // Thay đổi package nếu cần

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Định nghĩa Entity cho bảng "users"
// Thêm index unique cho username để đảm bảo không trùng lặp
@Entity(tableName = "users", indices = {@Index(value = {"username"}, unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id; // Khóa chính tự tăng

    @NonNull // Tên đăng nhập không được null và là duy nhất
    private String username;

    @NonNull // Tên đầy đủ không được null
    private String name;

    // Số điện thoại, có thể null nếu người dùng không nhập
    private String phone;

    @NonNull // Mật khẩu ĐÃ ĐƯỢC BĂM, không được null
    private String hashedPassword; // Lưu mật khẩu đã băm

    // Địa chỉ, có thể null nếu người dùng không nhập
    private String address;

    // --- Constructors ---
    // Constructor mặc định cần thiết cho Room
    public User() {}

    // Constructor tiện lợi để tạo đối tượng User mới (không bao gồm id tự tăng)
    public User(@NonNull String username, @NonNull String name, String phone, @NonNull String hashedPassword, String address) {
        this.username = username;
        this.name = name;
        this.phone = phone;
        this.hashedPassword = hashedPassword;
        this.address = address;
    }

    // --- Getters ---
    public int getId() { return id; }
    @NonNull public String getUsername() { return username; }
    @NonNull public String getName() { return name; }
    public String getPhone() { return phone; }
    @NonNull public String getHashedPassword() { return hashedPassword; }
    public String getAddress() { return address; }

    // --- Setters ---
    // Room cần setter cho id
    public void setId(int id) { this.id = id; }
    public void setUsername(@NonNull String username) { this.username = username; }
    public void setName(@NonNull String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setHashedPassword(@NonNull String hashedPassword) { this.hashedPassword = hashedPassword; }
    public void setAddress(String address) { this.address = address; }
}