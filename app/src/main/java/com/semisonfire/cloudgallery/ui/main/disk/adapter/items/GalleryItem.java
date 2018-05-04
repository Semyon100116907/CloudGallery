package com.semisonfire.cloudgallery.ui.main.disk.adapter.items;

import com.semisonfire.cloudgallery.data.model.Photo;

import java.util.ArrayList;
import java.util.List;

public class GalleryItem extends DiskItem {

    private String date;
    private List<Photo> photos;

    public GalleryItem() {
        photos = new ArrayList<>();
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    @Override
    public int getType() {
        return TYPE_GALLERY;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String getDate() {
        return date;
    }
}
