package com.semisonfire.cloudgallery.data.local;

import android.util.Log;

import com.semisonfire.cloudgallery.data.model.Photo;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class LocalDataSource {

    private LocalDatabase mLocalDatabase;

    public LocalDataSource(LocalDatabase localDatabase) {
        mLocalDatabase = localDatabase;
    }

    public Flowable<List<Photo>> getUploadingPhotos() {
        return mLocalDatabase.getPhotoDao().getUploadingPhotos().toFlowable();
    }

    public Flowable<Photo> saveUploadingPhoto(Photo photo) {
        photo.setUploaded(false);
        return savePhoto(photo);
    }

    public Completable removeUploadingPhoto(Photo photo) {
        return Completable.fromAction(() -> mLocalDatabase.getPhotoDao().deletePhoto(photo));
    }

    private Flowable<Photo> savePhoto(Photo photo) {
        return Completable.fromCallable(() -> {
            photo.setId(mLocalDatabase.getPhotoDao().insertPhoto(photo));
            return photo;
        })
                .subscribeOn(Schedulers.io())
                .andThen(Flowable.just(photo));
    }
}
