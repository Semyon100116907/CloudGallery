package com.semisonfire.cloudgallery.ui.trash;

import android.text.TextUtils;

import com.semisonfire.cloudgallery.core.presentation.BasePresenter;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.semisonfire.cloudgallery.ui.disk.DiskPresenter.LIMIT;

public class TrashPresenter extends BasePresenter<TrashContract.View>
        implements TrashContract.Presenter {

    private RemoteRepository remoteRepository;

    public TrashPresenter(RemoteRepository remoteRepository) {
        this.remoteRepository = remoteRepository;
    }

    @Override
    public void getPhotos(int page) {
        getCompositeDisposable().add(
                remoteRepository.getTrashPhotos(LIMIT, page)
                        .flatMapIterable(photos -> photos)
                        .filter(photo -> !TextUtils.isEmpty(photo.getMediaType()))
                        .filter(photo -> photo.getMediaType().equals("image"))
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                photos -> getView().onTrashLoaded(photos),
                                throwable -> getView().onError(throwable)
                        ));
    }

    @Override
    public void restorePhotos(List<? extends Photo> photos) {
        List<Photo> items = new ArrayList<>(photos);
        getCompositeDisposable().add(
                Flowable.fromIterable(items)
                        .concatMap(photo -> remoteRepository.restoreTrashPhoto(photo))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                p -> getView().onPhotoRestored(p),
                                throwable -> getView().onError(throwable)
                        ));
    }

    @Override
    public void deletePhotos(List<? extends Photo> photos) {
        List<Photo> items = new ArrayList<>(photos);
        getCompositeDisposable().add(
                Flowable.fromIterable(items)
                        .concatMap(photo -> remoteRepository.deleteTrashPhoto(photo))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                p -> getView().onPhotoDeleted(p),
                                throwable -> getView().onError(throwable)
                        ));
    }

    @Override
    public void clear() {
        getCompositeDisposable().add(
                remoteRepository.clearTrash()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> getView().onTrashCleared(),
                                throwable -> getView().onError(throwable)
                        ));
    }

}
