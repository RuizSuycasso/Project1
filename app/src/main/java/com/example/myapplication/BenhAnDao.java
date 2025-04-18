package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
// import androidx.lifecycle.LiveData;

@Dao
public interface BenhAnDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BenhAn benhAn);

    @Update
    void update(BenhAn benhAn);

    @Delete
    void delete(BenhAn benhAn);

    @Query("SELECT * FROM benhan_table ORDER BY id DESC")
    List<BenhAn> getAllBenhAn();

    // --- THÊM MỚI: Lấy một bệnh án theo ID ---
    @Query("SELECT * FROM benhan_table WHERE id = :id LIMIT 1")
    BenhAn getBenhAnById(int id);
    // ---------------------------------------

    /*
    @Query("SELECT * FROM benhan_table ORDER BY id DESC")
    LiveData<List<BenhAn>> getAllBenhAnLiveData();

    @Query("SELECT * FROM benhan_table WHERE id = :id LIMIT 1")
    LiveData<BenhAn> getBenhAnByIdLiveData(int id); // Phiên bản LiveData nếu cần
    */
}