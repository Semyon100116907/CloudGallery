package com.semisonfire.cloudgallery.ui.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteDataSource;
import com.semisonfire.cloudgallery.ui.base.BasePresenter;
import com.semisonfire.cloudgallery.utils.FileUtils;

import java.net.URL;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PhotoDetailPresenter<V extends PhotoDetailContract.View> extends BasePresenter<V>
        implements PhotoDetailContract.Presenter<V> {

    private RemoteDataSource mRemoteDataSource;

    public PhotoDetailPresenter(DiskPreferences diskPreferences, RemoteDataSource remoteDataSource) {
        super(diskPreferences);
        mRemoteDataSource = remoteDataSource;
    }

    @Override
    public void download(Photo photo) {
        getCompositeDisposable().add(
                mRemoteDataSource.getDownloadLink(photo)
                        .map(link -> {
                            URL url = new URL(link.getHref());
                            Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            return FileUtils.getInstance().savePublicFile(bitmap, photo.getName());
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(path -> getMvpView().onPhotoDownloaded(path),
                                throwable -> getMvpView().onError(throwable)));
    }

    @Override
    public void delete(Photo photo, int from) {

        Flowable<Photo> delete;

        switch (from) {
            case PhotoDetailActivity.FROM_DISK:
                delete = mRemoteDataSource.deletePhoto(photo);
                break;
            case PhotoDetailActivity.FROM_TRASH:
                delete = mRemoteDataSource.deleteTrashPhoto(photo);
                break;
            default:
                delete = Flowable.just(photo);
        }

        getCompositeDisposable().add(
                delete.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(p -> getMvpView().onFilesChanged(p),
                                throwable -> getMvpView().onError(throwable)));
    }

    @Override
    public void restore(Photo photo) {
        getCompositeDisposable().add(
                mRemoteDataSource.restoreTrashPhoto(photo)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(p -> getMvpView().onFilesChanged(p),
                                throwable -> getMvpView().onError(throwable)));
    }

    public void createShareFile(Bitmap bitmap) {
        getCompositeDisposable().add(
                Single.just(bitmap)
                        .map(btmp -> FileUtils.getInstance().createShareFile(btmp))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(uri -> getMvpView().onFilePrepared(uri),
                                throwable -> getMvpView().onError(throwable)));
    }
}
