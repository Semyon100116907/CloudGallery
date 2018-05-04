package com.semisonfire.cloudgallery.ui.main.disk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.semisonfire.cloudgallery.data.local.LocalDataSource;
import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteDataSource;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.data.remote.exceptions.InternetUnavailableException;
import com.semisonfire.cloudgallery.ui.base.BasePresenter;
import com.semisonfire.cloudgallery.utils.DateUtils;
import com.semisonfire.cloudgallery.utils.FileUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;


public class DiskPresenter<V extends DiskContract.View> extends BasePresenter<V>
        implements DiskContract.Presenter<V> {

    private static final String TAG = DiskPresenter.class.getSimpleName();

    //Data sources
    private RemoteDataSource mRemoteDataSource;
    private LocalDataSource mLocalDataSource;

    //Upload
    private PublishSubject<Photo> mUploadSubject = PublishSubject.create();
    private PublishSubject<Photo> mUploadSubjectInformer = PublishSubject.create();
    private Queue<Photo> mPhotos = new LinkedList<>();

    public DiskPresenter(DiskPreferences mDiskPreferences, RemoteDataSource remoteDataSource, LocalDataSource localDataSource) {
        super(mDiskPreferences);
        mRemoteDataSource = remoteDataSource;
        mLocalDataSource = localDataSource;
        createInformerSubject();
        createUploadSubj();
    }

    @Override
    public void getPhotos(int page) {
        Disposable result =
                mRemoteDataSource.getPhotos(DiskClient.MAX_LIMIT, page)
                        /*.flatMapIterable(photos -> photos)
                        .toList()*/
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(photos -> getMvpView().onPhotosLoaded(photos),
                                throwable -> getMvpView().onError(throwable));
        getCompositeDisposable().add(result);
    }

    public void downloadPhotos(List<Photo> photos) {
        List<Photo> items = new ArrayList<>(photos);
        getCompositeDisposable().add(
                Flowable.fromIterable(items)
                        .concatMap(photo -> mRemoteDataSource.getDownloadLink(photo))
                        .map(link -> {
                            URL url = new URL(link.getHref());
                            Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            return FileUtils.getInstance().savePublicFile(bitmap, link.getPhoto().getName());
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(path -> getMvpView().onPhotoDownloaded(path),
                                throwable -> getMvpView().onError(throwable)));
    }

    public void deletePhotos(List<Photo> photos) {
        List<Photo> items = new ArrayList<>(photos);
        getCompositeDisposable().add(
                Flowable.fromIterable(items)
                        .concatMap(photo -> mRemoteDataSource.deletePhoto(photo))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(p -> getMvpView().onPhotoDeleted(p),
                                throwable -> getMvpView().onError(throwable)));
    }

    @Override
    public void uploadPhoto(Photo photo) {
        mPhotos.add(photo);
        uploadNext();
    }

    @Override
    public void uploadPhotos(List<Photo> photos) {
        mPhotos.addAll(photos);
        uploadNext();
    }

    private void createInformerSubject() {
        getCompositeDisposable().add(
                mUploadSubjectInformer
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(photo -> getMvpView().onPhotoUploaded(null),
                                throwable -> getMvpView().onError(throwable)));
    }

    private void createUploadSubj() {
        getCompositeDisposable().add(
                mUploadSubject.subscribeOn(Schedulers.io())
                        .concatMap(photo -> getUploadFlowable(photo).toObservable())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(photo -> getMvpView().onPhotoUploaded(photo),
                                throwable -> getMvpView().onError(throwable)));
    }

    private Flowable<Photo> getUploadFlowable(Photo p) {
        return Flowable.just(p)
                /*add local rows*/
                .concatMap(photo -> Flowable.just(photo).delay(100, TimeUnit.MILLISECONDS))
                .flatMap(photo -> mRemoteDataSource.getUploadLink(photo))
                .flatMap(link -> mRemoteDataSource.savePhoto(link.getPhoto(), link))
                .retryWhen(error -> error.flatMap(throwable -> {
                    mUploadSubjectInformer.onNext(new Photo());
                    if (throwable instanceof InternetUnavailableException) {
                        Log.d(TAG, "getUploadFlowable: Retrying...");
                        return Flowable.timer(5, TimeUnit.SECONDS);
                    }
                    return Flowable.error(throwable);
                }))
                .filter(Photo::isUploaded)
                .map(photo -> {
                    photo.setModifiedAt(DateUtils.getCurrentDate());
                    return photo;
                })
                /*update local rows*/;
    }

    private void uploadNext() {
        while (!mPhotos.isEmpty()) {
            mUploadSubject.onNext(mPhotos.remove());
        }
    }
}
