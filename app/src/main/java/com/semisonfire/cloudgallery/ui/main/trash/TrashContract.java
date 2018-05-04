package com.semisonfire.cloudgallery.ui.main.trash;

import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.ui.base.MvpPresenter;
import com.semisonfire.cloudgallery.ui.base.MvpView;

import java.util.List;

public interface TrashContract {

    interface View extends MvpView {

        void onTrashLoaded(List<Photo> photos);

        void onPhotoDeleted(Photo photo);

        void onPhotoRestored(Photo photo);

        void onTrashCleared();

    }

    interface Presenter<V extends View> extends MvpPresenter<V> {

        void getPhotos(int page);

        void restorePhotos(List<Photo> photos);

        void deletePhotos(List<Photo> photos);

        void clear();
    }

}
