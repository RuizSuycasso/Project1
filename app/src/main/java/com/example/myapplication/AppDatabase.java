package com.example.myapplication;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService; // Thêm import
import java.util.concurrent.Executors;     // Thêm import

@Database(entities = {BenhAn.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;
    public abstract BenhAnDao benhAnDao();

    // --- THÊM MỚI: ExecutorService để chạy DB operations trên background thread ---
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    // -----------------------------------------------------------------------

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "benh_an_database")
                            .fallbackToDestructiveMigration()
                            // --- XÓA DÒNG NÀY ---
                            // .allowMainThreadQueries()
                            // --------------------
                            .build();
                }
            }
        }
        return instance;
    }
}