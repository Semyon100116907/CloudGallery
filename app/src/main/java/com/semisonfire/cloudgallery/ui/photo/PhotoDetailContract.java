package com.semisonfire.cloudgallery.ui.photo;

import android.net.Uri;

import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.ui.base.MvpPresenter;
import com.semisonfire.cloudgallery.ui.base.MvpView;

public interface PhotoDetailContract {

    interface View extends MvpView {

        void onPhotoDownloaded(String path);

        void onFilePrepared(Uri uri);

        void onFilesChanged(Photo photo);
    }

    interface Presenter<V extends View> extends MvpPresenter<V> {

        void download(Photo photo);

        void delete(Photo photo, int from);

        void restore(Photo photo);

    }
}
