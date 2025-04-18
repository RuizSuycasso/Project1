package com.example.myapplication; // Thay đổi package nếu cần

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao // Đánh dấu đây là một Data Access Object
public interface UserDAO {

    /**
     * Chèn một user mới vào database.
     * Nếu username đã tồn tại (do có index unique), việc chèn sẽ bị bỏ qua.
     * @param user Đối tượng User cần chèn.
     * @return rowId của bản ghi mới được chèn, hoặc -1 nếu bị bỏ qua do xung đột.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(User user); // Nên chạy trên luồng nền

    /**
     * Tìm một user trong database dựa vào username.
     * @param username Tên đăng nhập cần tìm.
     * @return Đối tượng User nếu tìm thấy, hoặc null nếu không tìm thấy.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username); // Nên chạy trên luồng nền

    /**
     * Lấy tên đầy đủ (name) của user dựa vào username.
     * Hữu ích cho việc hiển thị thông tin mà không cần load cả object User.
     * @param username Tên đăng nhập của user.
     * @return Tên đầy đủ (String) của user nếu tìm thấy, hoặc null nếu không tìm thấy.
     */
    @Query("SELECT name FROM users WHERE username = :username LIMIT 1")
    String getNameByUsername(String username); // Nên chạy trên luồng nền

}