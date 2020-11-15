package com.goody.myapplication.roomdb;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WishListDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(WishListDto wishlist);

    @Delete
    void delete(WishListDto wishlist);

    @Update
    void update(WishListDto wishlist);

    @Query("SELECT * FROM WISHLIST ORDER BY time DESC")
    LiveData<List<WishListDto>> loadAllWishList();
}
