package com.example.myapplication;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Đảm bảo các entity được liệt kê chính xác
@Database(entities = {BenhAn.class}, version = 1, exportSchema = false) // Thêm exportSchema = false để tránh cảnh báo build
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance; // Thêm 'volatile' cho an toàn luồng tốt hơn trong Singleton

    public abstract BenhAnDao benhAnDao();

    public static AppDatabase getInstance(Context context) {
        // Double-checked locking pattern cho Singleton
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "benh_an_database")
                            // Migration strategy: Nếu không cung cấp migration, DB sẽ bị xóa và tạo lại khi tăng version.
                            .fallbackToDestructiveMigration()
                            // --- CẢNH BÁO: Cho phép truy vấn trên luồng chính ---
                            // Dòng này cho phép gọi các hàm DAO (insert, query, update, delete)
                            // trực tiếp trên Main Thread. Điều này KHÔNG được khuyến khích
                            // vì có thể gây treo ứng dụng (ANR). Nên xóa dòng này và
                            // sử dụng giải pháp bất đồng bộ (Coroutines, RxJava,...)
                            .allowMainThreadQueries()
                            // ------------------------------------------------------
                            .build();
                }
            }
        }
        return instance;
    }
}