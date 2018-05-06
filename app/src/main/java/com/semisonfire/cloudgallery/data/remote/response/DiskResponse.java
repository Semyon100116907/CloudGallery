package com.semisonfire.cloudgallery.data.remote.response;

import com.google.gson.annotations.SerializedName;
import com.semisonfire.cloudgallery.data.model.Photo;

import java.util.List;

public class DiskResponse {

    @SerializedName("items")
    private List<Photo> photos = null;

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

}
