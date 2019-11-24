package com.semisonfire.cloudgallery.ui.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.semisonfire.cloudgallery.core.presentation.BasePresenter;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteRepository;
import com.semisonfire.cloudgallery.di.ActivityScope;
import com.semisonfire.cloudgallery.utils.FileUtils;

import java.net.URL;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@ActivityScope
public class PhotoDetailPresenter extends BasePresenter<PhotoDetailContract.View>
        implements PhotoDetailContract.Presenter {

    private RemoteRepository remoteRepository;

    @Inject
    public PhotoDetailPresenter(RemoteRepository remoteRepository) {
        this.remoteRepository = remoteRepository;
    }

    @Override
    public void download(Photo photo) {
        getCompositeDisposable().add(
                remoteRepository.getDownloadLink(photo)
                        .map(link -> {
                            URL url = new URL(link.getHref());
                            Bitmap bitmap = BitmapFactory
                                    .decodeStream(url.openConnection().getInputStream());
                            return FileUtils.getInstance().savePublicFile(bitmap, photo.getName());
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                path -> getView().onPhotoDownloaded(path),
                                throwable -> getView().onError(throwable)
                        ));
    }

    @Override
    public void delete(Photo photo, int from) {

        Flowable<Photo> delete;

        switch (from) {
            case PhotoDetailActivity.FROM_DISK:
                delete = remoteRepository.deletePhoto(photo);
                break;
            case PhotoDetailActivity.FROM_TRASH:
                delete = remoteRepository.deleteTrashPhoto(photo);
                break;
            default:
                delete = Flowable.just(photo);
        }

        getCompositeDisposable().add(
                delete.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                p -> getView().onFilesChanged(p),
                                throwable -> getView().onError(throwable)
                        ));
    }

    @Override
    public void restore(Photo photo) {
        getCompositeDisposable().add(
                remoteRepository.restoreTrashPhoto(photo)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                p -> getView().onFilesChanged(p),
                                throwable -> getView().onError(throwable)
                        ));
    }

    @Override
    public void createShareFile(Bitmap bitmap) {
        getCompositeDisposable().add(
                Single.just(bitmap)
                        .map(btmp -> FileUtils.getInstance().createShareFile(btmp))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                uri -> getView().onFilePrepared(uri),
                                throwable -> getView().onError(throwable)
                        ));
    }
}
