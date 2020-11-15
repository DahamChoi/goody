package com.goody.myapplication.roomdb;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "wishlist")
public class WishListDto {
    @NonNull
    @PrimaryKey
    public String title;

    public String mallname;
    public String price;
    public String imgurl;
    public String link;

    public Date time;

}
