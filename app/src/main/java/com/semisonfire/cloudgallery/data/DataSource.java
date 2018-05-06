package com.semisonfire.cloudgallery.data;

import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.response.Link;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface DataSource {

    Flowable<List<Photo>> getPhotos(int limit, int page);

    Flowable<List<Photo>> getTrashPhotos(int limit, int page);

    Flowable<Photo> savePhoto(Photo photo, Link link);

    Flowable<Photo> deletePhoto(Photo photo);

    Completable clearTrash();
}
