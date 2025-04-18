package com.example.myapplication; // Thay đổi package nếu cần

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Đánh dấu đây là class Database, liệt kê các Entities và đặt version
@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    // Phương thức abstract để Room cung cấp instance của DAO
    public abstract UserDAO userDao();

    // Biến static volatile để giữ instance duy nhất (Singleton)
    private static volatile UserDatabase INSTANCE;

    // Tên file cho database SQLite
    private static final String DATABASE_NAME = "app_user_database.db"; // Đặt tên khác biệt một chút

    // Phương thức static để lấy instance duy nhất của Database (Thread-safe)
    public static UserDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UserDatabase.class, DATABASE_NAME)
                            // CẢNH BÁO: Chỉ dùng cho phát triển.
                            // Sẽ xóa và tạo lại DB nếu version tăng mà không có Migration.
                            // Nên thay thế bằng addMigrations() cho ứng dụng thực tế.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // (Tùy chọn) Phương thức để đóng database nếu thực sự cần thiết
    public static void closeInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
        INSTANCE = null; // Reset instance
    }
}