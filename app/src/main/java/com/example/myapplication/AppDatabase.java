package com.example.myapplication;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Đảm bảo import các Entities và DAOs
// import com.example.myapplication.BenhAn; // Đã import ngầm
// import com.example.myapplication.User;   // Đã import ngầm
// import com.example.myapplication.BenhAnDao; // Đã import ngầm
// import com.example.myapplication.UserDAO;   // Đã import ngầm

// <<< THAY ĐỔI CHÍNH >>>
// 1. Thêm User.class vào entities
// 2. Tăng version (ví dụ lên 2, nếu bạn đã có DB cũ version 1)
//    Hoặc đặt là 1 nếu đây là lần đầu tạo DB với cấu trúc này.
@Database(entities = {BenhAn.class, User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    // Tên file DB, bạn có thể giữ "benh_an_database" hoặc đổi tên chung hơn
    private static final String DATABASE_NAME = "my_unified_database.db";

    public abstract BenhAnDao benhAnDao();
    public abstract UserDAO userDao(); // <<< THÊM: Cung cấp UserDAO

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration() // Giữ lại khi đang phát triển
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // (Tùy chọn) Đóng instance nếu cần
    public static void closeInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
        INSTANCE = null;
    }
}