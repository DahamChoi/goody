package com.goody.myapplication.roomdb;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "searchlist")
public class SearchListDto {
    @NonNull
    @PrimaryKey
    public String name;

    @ColumnInfo(name = "mallname")
    public String mallname;

    @ColumnInfo(name = "time")
    public Date time;

    public SearchListDto(String name,String mallname,Date time){
        this.name = name;
        this.mallname = mallname;
        this.time = time;
    }
}
