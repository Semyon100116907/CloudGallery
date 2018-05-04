package com.semisonfire.cloudgallery.data.local;

import com.semisonfire.cloudgallery.data.model.Photo;

import java.util.List;

import io.reactivex.Flowable;

public class LocalDataSource {

    private LocalDatabase mLocalDatabase;

    public LocalDataSource(LocalDatabase localDatabase) {
        mLocalDatabase = localDatabase;
    }

    public Flowable<List<Photo>> getPhotos() {
        return mLocalDatabase.getPhotoDao().getAllPhotos();
    }

    public Flowable<Photo> savePhoto(Photo photo) {
        return Flowable.just(photo)
                .map(p -> {
                    mLocalDatabase.getPhotoDao().insertPhoto(p);
                    return p;
                });
    }

    public void updatePhoto(Photo photo) {
        mLocalDatabase.getPhotoDao().updatePhoto(photo);
    }

    public void saveUploadingPhoto(Photo photo) {
        photo.setUploaded(false);
        savePhoto(photo);
    }

    public void updateUploadingPhoto(Photo photo) {
        photo.setUploaded(true);
        updatePhoto(photo);
    }
}
