package com.goody.myapplication;

public class Item {

    public String image;
    public String mallName;
    public String price;
    public String link;
    public String title;

    public String category1;
    public String category2;
    public String category3;
    public String category4;

    public Item(){}

    public Item(String title, String mallName, String image, String url, String price,
                String category1,String category2,String category3,String category4){
        this.title = title;
        this.image = image;
        this.link = url;
        this.price = price;
        this.mallName = mallName;

        this.category1 = category1;
        this.category2 = category2;
        this.category3 = category3;
        this.category4 = category4;
    }

    public Item(String title, String mallName, String image, String url, String price) {
        this.title = title;
        this.image = image;
        this.link = url;
        this.price = price;
        this.mallName = mallName;
    }
}
