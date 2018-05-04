package com.semisonfire.cloudgallery.ui.main.trash;

import android.text.TextUtils;

import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteDataSource;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TrashPresenter<V extends TrashContract.View> extends BasePresenter<V>
        implements TrashContract.Presenter<V> {

    private RemoteDataSource mRemoteDataSource;

    public TrashPresenter(DiskPreferences preferences, RemoteDataSource remoteDataSource) {
        super(preferences);
        mRemoteDataSource = remoteDataSource;
    }

    @Override
    public void getPhotos(int page) {
        getCompositeDisposable().add(
                mRemoteDataSource.getTrashPhotos(DiskClient.MAX_LIMIT, page)
                        .flatMapIterable(photos -> photos)
                        .filter(photo -> !TextUtils.isEmpty(photo.getMediaType()))
                        .filter(photo -> photo.getMediaType().equals("image"))
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(photos -> getMvpView().onTrashLoaded(photos),
                                throwable -> getMvpView().onError(throwable)));
    }

    @Override
    public void restorePhotos(List<Photo> photos) {
        List<Photo> items = new ArrayList<>(photos);
        getCompositeDisposable().add(
                Flowable.fromIterable(items)
                        .concatMap(photo -> mRemoteDataSource.restoreTrashPhoto(photo))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(p -> getMvpView().onPhotoRestored(p),
                                throwable -> getMvpView().onError(throwable)));
    }

    @Override
    public void deletePhotos(List<Photo> photos) {
        List<Photo> items = new ArrayList<>(photos);
        getCompositeDisposable().add(
                Flowable.fromIterable(items)
                        .concatMap(photo -> mRemoteDataSource.deleteTrashPhoto(photo))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(p -> getMvpView().onPhotoDeleted(p),
                                throwable -> getMvpView().onError(throwable)));
    }

    @Override
    public void clear() {
        getCompositeDisposable().add(
                mRemoteDataSource.clearTrash()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> getMvpView().onTrashCleared(),
                                throwable -> getMvpView().onError(throwable)));
    }

}
