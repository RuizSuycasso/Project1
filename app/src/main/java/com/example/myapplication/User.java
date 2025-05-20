package com.example.myapplication; // Thay đổi package nếu cần

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"username"}, unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String username;

    @NonNull
    private String name;

    private String phone;

    @NonNull
    private String hashedPassword;

    private String address;

    public User() {}

    public User(@NonNull String username, @NonNull String name, String phone, @NonNull String hashedPassword, String address) {
        this.username = username;
        this.name = name;
        this.phone = phone;
        this.hashedPassword = hashedPassword;
        this.address = address;
    }

    public int getId() { return id; }
    @NonNull public String getUsername() { return username; }
    @NonNull public String getName() { return name; }
    public String getPhone() { return phone; }
    @NonNull public String getHashedPassword() { return hashedPassword; }
    public String getAddress() { return address; }

    public void setId(int id) { this.id = id; }
    public void setUsername(@NonNull String username) { this.username = username; }
    public void setName(@NonNull String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setHashedPassword(@NonNull String hashedPassword) { this.hashedPassword = hashedPassword; }
    public void setAddress(String address) { this.address = address; }
}