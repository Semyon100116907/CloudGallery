package com.semisonfire.cloudgallery.ui.main.disk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
import retrofit2.HttpException;


public class DiskPresenter<V extends DiskContract.View> extends BasePresenter<V>
        implements DiskContract.Presenter<V> {

    private static final String TAG = DiskPresenter.class.getSimpleName();

    //Data sources
    private RemoteDataSource mRemoteDataSource;
    private LocalDataSource mLocalDataSource;

    //Upload
    private PublishSubject<Photo> mUploadSubject = PublishSubject.create();
    private PublishSubject<Photo> mUploadSubjectInformer = PublishSubject.create();
    private Queue<Photo> mUploadingPhotos = new LinkedList<>();

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
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(photos -> getMvpView().onPhotosLoaded(photos),
                                throwable -> getMvpView().onError(throwable));
        getCompositeDisposable().add(result);
    }

    public void getUploadingPhotos() {
        getCompositeDisposable().add(
                mLocalDataSource.getUploadingPhotos()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(photos -> getMvpView().onUploadingPhotos(photos),
                                throwable -> getMvpView().onError(throwable)));
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
        mUploadingPhotos.add(photo);
        uploadNext();
    }

    @Override
    public void uploadPhotos(List<Photo> photos) {
        mUploadingPhotos.addAll(photos);
        uploadNext();
    }

    private void uploadNext() {
        while (!mUploadingPhotos.isEmpty()) {
            mUploadSubject.onNext(mUploadingPhotos.remove());
        }
    }

    /**
     * Subject which inform android main thread about error
     */
    private void createInformerSubject() {
        getCompositeDisposable().add(
                mUploadSubjectInformer
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(photo -> getMvpView().onPhotoUploaded(null),
                                throwable -> getMvpView().onError(throwable)));
    }

    private void createUploadSubj() {
        getCompositeDisposable().add(
                mUploadSubject
                        .flatMap(photo -> mLocalDataSource.saveUploadingPhoto(photo).toObservable())
                        .concatMap(photo -> getUploadFlowable(photo).toObservable())
                        .subscribe(photo -> getMvpView().onPhotoUploaded(photo),
                                throwable -> getMvpView().onError(throwable)));
    }

    /**
     * Upload one photo at the time.
     */
    private Flowable<Photo> getUploadFlowable(Photo p) {
        String beginName = p.getName();
        return Flowable.just(p)
                .subscribeOn(Schedulers.io())
                .concatMap(photo -> Flowable.just(photo).delay(1000, TimeUnit.MILLISECONDS))
                .flatMap(photo -> mRemoteDataSource.getUploadLink(photo))
                .flatMap(link -> mRemoteDataSource.savePhoto(link.getPhoto(), link))
                .retryWhen(error -> error.flatMap(throwable -> {
                    if (throwable instanceof InternetUnavailableException) {
                        mUploadSubjectInformer.onNext(new Photo());
                        return Flowable.timer(2, TimeUnit.SECONDS);
                    }
                    if (throwable instanceof HttpException) {
                        if (((HttpException) throwable).code() == 409) {
                            p.setName(rename(p.getName()));
                            return Flowable.timer(1, TimeUnit.SECONDS);
                        }
                    }
                    return Flowable.error(throwable);
                }))
                .filter(Photo::isUploaded)
                .flatMap(photo -> {
                    photo.setModifiedAt(DateUtils.getCurrentDate());
                    photo.setName(beginName);
                    return mLocalDataSource.updateUploadingPhoto(photo);
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /** Rename duplicate file. */
    private String rename(String fullName) {
        int index = fullName.lastIndexOf('.');
        String name = fullName.substring(0, index);
        String extension = fullName.substring(index);
        String newName;
        int first = name.indexOf("(") + 1;
        int last = name.lastIndexOf(")");
        if (first != -1 && last != -1) {
            int counter = Integer.parseInt(name.substring(first, last));
            counter++;
            newName = name.substring(0, first - 1) + "(" + String.valueOf(counter) + ")";
        } else {
            newName = name + "(1)";
        }
        newName += extension;
        return newName;
    }
}
