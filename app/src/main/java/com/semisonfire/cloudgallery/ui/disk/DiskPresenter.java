package com.semisonfire.cloudgallery.ui.disk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.semisonfire.cloudgallery.core.presentation.BasePresenter;
import com.semisonfire.cloudgallery.data.local.LocalRepository;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteRepository;
import com.semisonfire.cloudgallery.data.remote.exceptions.InternetUnavailableException;
import com.semisonfire.cloudgallery.utils.DateUtils;
import com.semisonfire.cloudgallery.utils.FileUtils;

import java.io.File;
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


public class DiskPresenter extends BasePresenter<DiskContract.View>
        implements DiskContract.Presenter {

    public static final int LIMIT = 15;

    //Data sources
    private RemoteRepository remoteRepository;
    private LocalRepository localRepository;

    //Upload
    private PublishSubject<Photo> mUploadSubject = PublishSubject.create();
    private PublishSubject<Photo> mUploadSubjectInformer = PublishSubject.create();
    private Queue<Photo> mUploadingPhotos = new LinkedList<>();

    public DiskPresenter(RemoteRepository remoteRepository, LocalRepository localRepository) {
        this.remoteRepository = remoteRepository;
        this.localRepository = localRepository;
        createInformerSubject();
        createUploadSubj();
    }

    @Override
    public void getPhotos(int page) {
        Disposable result =
                remoteRepository.getPhotos(LIMIT, page)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                photos -> getView().onPhotosLoaded(photos),
                                throwable -> getView().onError(throwable)
                        );
        getCompositeDisposable().add(result);
    }

    @Override
    public void getUploadingPhotos() {
        getCompositeDisposable().add(
                localRepository.getUploadingPhotos()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                photos -> getView().onUploadingPhotos(photos),
                                throwable -> getView().onError(throwable)
                        ));
    }

    @Override
    public void downloadPhotos(List<? extends Photo> photos) {
        List<Photo> items = new ArrayList<>(photos);
        getCompositeDisposable().add(
                Flowable.fromIterable(items)
                        .concatMap(photo -> remoteRepository.getDownloadLink(photo))
                        .map(link -> {
                            URL url = new URL(link.getHref());
                            Bitmap bitmap = BitmapFactory
                                    .decodeStream(url.openConnection().getInputStream());
                            return FileUtils.getInstance()
                                    .savePublicFile(bitmap, link.getPhoto().getName());
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                path -> getView().onPhotoDownloaded(path),
                                throwable -> getView().onError(throwable)
                        ));
    }

    @Override
    public void deletePhotos(List<? extends Photo> photos) {
        List<Photo> items = new ArrayList<>(photos);
        getCompositeDisposable().add(
                Flowable.fromIterable(items)
                        .concatMap(photo -> remoteRepository.deletePhoto(photo))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                p -> getView().onPhotoDeleted(p),
                                throwable -> getView().onError(throwable)
                        ));
    }

    @Override
    public void uploadPhoto(Photo photo) {
        mUploadingPhotos.add(photo);
        uploadNext();
    }

    @Override
    public void uploadPhotos(List<? extends Photo> photos) {
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
                        .subscribe(
                                photo -> getView().onPhotoUploaded(photo, false),
                                throwable -> getView().onError(throwable)
                        ));
    }

    private void createUploadSubj() {
        getCompositeDisposable().add(
                mUploadSubject
                        .flatMap(photo -> localRepository.saveUploadingPhoto(photo).toObservable())
                        .concatMap(photo -> getUploadFlowable(photo).toObservable())
                        .subscribe(
                                photo -> getView().onPhotoUploaded(photo, true),
                                throwable -> getView().onError(throwable)
                        ));
    }

    /**
     * Upload one photo at the time.
     */
    private Flowable<Photo> getUploadFlowable(Photo p) {
        return Flowable.just(p)
                .subscribeOn(Schedulers.io())
                .concatMap(photo -> Flowable.just(photo).delay(1000, TimeUnit.MILLISECONDS))
                .flatMap(photo -> remoteRepository.getUploadLink(photo))
                .flatMap(link -> remoteRepository.savePhoto(link.getPhoto(), link))
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
                    String absolutePath =
                            "file://" + new File(photo.getLocalPath()).getAbsolutePath();
                    photo.setPreview(absolutePath);
                    return localRepository.removeUploadingPhoto(photo)
                            .andThen(Flowable.just(photo));
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Rename duplicate file.
     */
    private String rename(String fullName) {
        int index = fullName.lastIndexOf('.');
        String name = fullName.substring(0, index);
        String extension = fullName.substring(index);
        String newName;
        int first = name.indexOf("(") + 1;
        int last = name.lastIndexOf(")");
        if (last != -1) {
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
