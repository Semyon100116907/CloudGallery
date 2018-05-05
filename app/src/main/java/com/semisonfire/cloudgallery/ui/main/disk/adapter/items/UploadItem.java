package com.semisonfire.cloudgallery.ui.main.disk.adapter.items;

import com.semisonfire.cloudgallery.data.model.Photo;

import java.util.ArrayList;
import java.util.List;

public class UploadItem extends DiskItem {

    private List<Photo> uploadPhotos;
    private int size;
    private int uploadCount;
    private int visibility;
    private String state;

    public UploadItem() {
        uploadPhotos = new ArrayList<>();
        uploadCount = 1;
    }

    public List<Photo> getUploadPhotos() {
        return uploadPhotos;
    }

    public void addUploadPhotos(List<Photo> photos) {
        uploadPhotos.addAll(photos);
        size = uploadPhotos.size();
    }

    public void resetUploadCount() {
        this.uploadCount = 1;
    }

    public void incrementUpload() {
        ++uploadCount;
    }

    @Override
    public int getType() {
        return TYPE_UPLOAD;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getUploadCount() {
        return uploadCount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof UploadItem) {
            return getType() == ((UploadItem) obj).getType();
        }

        return super.equals(obj);
    }
}
