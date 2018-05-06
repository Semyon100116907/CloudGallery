package com.semisonfire.cloudgallery.data.remote.response;

import android.arch.persistence.room.Ignore;

import com.google.gson.annotations.SerializedName;
import com.semisonfire.cloudgallery.data.model.Photo;

public class Link {

    @SerializedName("href")
    private String href;

    @Ignore
    private Photo photo;

    public String getHref() {
        return href;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public Photo getPhoto() {
        return photo;
    }
}
