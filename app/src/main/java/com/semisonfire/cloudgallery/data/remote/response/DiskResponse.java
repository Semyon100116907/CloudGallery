package com.semisonfire.cloudgallery.data.remote.response;

import com.google.gson.annotations.SerializedName;
import com.semisonfire.cloudgallery.data.model.Photo;

import java.util.List;

public class DiskResponse {

    @SerializedName("items")
    private List<Photo> photos = null;
    @SerializedName("limit")
    private Integer limit;

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

}
