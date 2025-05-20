package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update; // <<< PHẢI CÓ IMPORT NÀY

@Dao
public interface UserDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(User user);

    // <<< PHƯƠNG THỨC UPDATE NÀY PHẢI CÓ >>>
    @Update
    void update(User user);
    // ------------------------------------

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    @Query("SELECT name FROM users WHERE username = :username LIMIT 1")
    String getNameByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User findById(int userId);
}