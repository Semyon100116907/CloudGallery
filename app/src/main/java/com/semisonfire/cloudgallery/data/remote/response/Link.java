package com.semisonfire.cloudgallery.data.remote.response;

import android.arch.persistence.room.Ignore;

import com.google.gson.annotations.SerializedName;
import com.semisonfire.cloudgallery.data.model.Photo;

public class Link {

    @SerializedName("href")
    private String href;

    @SerializedName("method")
    private String method;

    @SerializedName("templated")
    private boolean templated;

    @Ignore
    private Photo photo;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isTemplated() {
        return templated;
    }

    public void setTemplated(boolean templated) {
        this.templated = templated;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public Photo getPhoto() {
        return photo;
    }
}
