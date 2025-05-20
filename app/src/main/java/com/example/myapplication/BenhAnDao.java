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

    // Đổi tên để rõ ràng hơn, hoặc xóa nếu không dùng
    @Query("SELECT * FROM benhan_table ORDER BY id DESC")
    List<BenhAn> getAllBenhAn_ForAdminOrDebug();

    @Query("SELECT * FROM benhan_table WHERE id = :id LIMIT 1")
    BenhAn getBenhAnById(int id);

    // --- THÊM MỚI: Lấy danh sách bệnh án theo userId ---
    @Query("SELECT * FROM benhan_table WHERE userId = :userId ORDER BY id DESC")
    List<BenhAn> getBenhAnByUserId(int userId);
    // --------------------------------------------------

    /*
    // Nếu dùng LiveData:
    @Query("SELECT * FROM benhan_table WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<BenhAn>> getBenhAnByUserIdLiveData(int userId);
    */
}