package com.example.teleprompter.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FileDao {

    @Query("SELECT * FROM files ORDER BY date")
    LiveData<List<File>> loadAllFiles();

    @Insert
    void insertFile(File file);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFile(File file);

    @Delete
    void deleteFile(File file);

    @Query("SELECT * FROM files WHERE id = :id")
    LiveData<File> loadFileById(int id);

}