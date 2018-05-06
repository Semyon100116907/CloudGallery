package com.semisonfire.cloudgallery.ui.main.disk;

import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.ui.base.MvpPresenter;
import com.semisonfire.cloudgallery.ui.base.MvpView;

import java.util.List;

public interface DiskContract {

    interface View extends MvpView {

        void onPhotosLoaded(List<Photo> photos);

        void onUploadingPhotos(List<Photo> photos);

        void onPhotoUploaded(Photo photo);

        void onPhotoDownloaded(String path);

        void onPhotoDeleted(Photo photo);
    }

    interface Presenter<V extends View> extends MvpPresenter<V> {

        void getPhotos(int offset);

        void uploadPhotos(List<Photo> photos);

        void uploadPhoto(Photo photo);

    }

}
