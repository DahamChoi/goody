package com.goody.myapplication.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SearchListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SearchListDto searchList);

    @Delete
    void delete(SearchListDto news);

    @Query("DELETE FROM SEARCHLIST")
    void deltetAllData();

    @Query("SELECT * FROM SEARCHLIST ORDER BY time DESC")
    List<SearchListDto> loadAllSearchList();
}
