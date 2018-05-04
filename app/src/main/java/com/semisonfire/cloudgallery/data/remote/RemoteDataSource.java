package com.semisonfire.cloudgallery.data.remote;

import com.semisonfire.cloudgallery.data.DataSource;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.api.DiskApi;
import com.semisonfire.cloudgallery.data.remote.response.DiskResponse;
import com.semisonfire.cloudgallery.data.remote.response.Link;

import java.io.File;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RemoteDataSource implements DataSource {

    private DiskApi mDiskApi;

    public RemoteDataSource(DiskApi diskApi) {
        mDiskApi = diskApi;
    }

    @Override
    public Flowable<List<Photo>> getPhotos(int limit, int page) {
        return mDiskApi.getDiskImages(limit, limit * (page - 1), "image", "XL", "-modified")
                .flatMapIterable(DiskResponse::getPhotos)
                .toList()
                .toFlowable();
    }

    @Override
    public Flowable<Photo> deletePhoto(Photo photo) {
        return mDiskApi.deleteImage(photo.getRemotePath())
                .andThen(Flowable.just(photo)
                        .map(p -> {
                            p.setRemoved(true);
                            return p;
                        }));
    }

    public Flowable<Link> getUploadLink(Photo photo) {
        return mDiskApi.getUploadLink("disk:/" + photo.getName(), false)
                .map(link -> {
                    link.setPhoto(photo);
                    return link;
                });
    }

    @Override
    public Flowable<Photo> savePhoto(Photo photo, Link link) {
        File file = new File(photo.getLocalPath());
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);
        return mDiskApi.uploadImage(link.getHref(), body)
                .andThen(Flowable.just(photo)
                        .map(p -> {
                            p.setUploaded(true);
                            p.setRemotePath("disk:/" + p.getName());
                            return p;
                        }));
    }

    @Override
    public Flowable<List<Photo>> getTrashPhotos(int limit, int page) {
        return mDiskApi.getTrashFiles("trash:/", limit, limit * (page - 1), "XL", "-deleted")
                .concatMapIterable(trash -> trash.getTrashResponse().getPhotos())
                .toList()
                .toFlowable();
    }

    public Flowable<Link> getDownloadLink(Photo photo) {
        return mDiskApi.getDownloadLink(photo.getRemotePath())
                .map(link -> {
                    link.setPhoto(photo);
                    return link;
                });
    }

    public Flowable<Photo> restoreTrashPhoto(Photo photo) {
        return mDiskApi.restorePhoto(photo.getRemotePath())
                .andThen(Flowable.just(photo));
    }

    public Flowable<Photo> deleteTrashPhoto(Photo photo) {
        return mDiskApi.deletePermanently(photo.getRemotePath())
                .andThen(Flowable.just(photo));
    }

    public Completable clearTrash() {
        return mDiskApi.deletePermanently(null);
    }
}
