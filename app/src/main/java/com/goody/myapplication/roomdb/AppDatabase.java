package com.goody.myapplication.roomdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {WishListDto.class, SearchListDto.class}, version=1)
@TypeConverters(DataConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SearchListDao searchListDao();
    public abstract WishListDao wishListDao();

    private static AppDatabase mAppDatabase;

    // 싱글튼 패턴을 유지해야 데이터의 일치성을 보장할 수 있다
    public static AppDatabase getInstance(Context context) {
        if (mAppDatabase == null) {
            mAppDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                    "goddy").allowMainThreadQueries().build();
        }
        return mAppDatabase;
    }
}