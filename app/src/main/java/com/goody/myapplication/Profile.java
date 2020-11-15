package com.goody.myapplication;

public class Profile {
    public Long id;
    public String profileNickName;
    public String profileThumbnailImage;

    public Profile() { }

    public Profile(long id,String profileNickName,String profileThumbnailImage){
        this.id = id;
        this.profileNickName = profileNickName;
        this.profileThumbnailImage = profileThumbnailImage;
    }
}
