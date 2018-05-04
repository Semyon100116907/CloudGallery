package com.semisonfire.cloudgallery.data.remote.api;

import android.support.annotation.Nullable;

import com.semisonfire.cloudgallery.data.remote.response.DiskResponse;
import com.semisonfire.cloudgallery.data.remote.response.Link;
import com.semisonfire.cloudgallery.data.remote.response.Trash;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface DiskApi {

    /** Get images from all disk folders besides trash folder. */
    @GET("disk/resources/files")
    Flowable<DiskResponse> getDiskImages(@Query("limit") int limit,
                                         @Query("offset") int offset,
                                         @Query("media_type") String mediaType,
                                         @Query("preview_size") String size,
                                         @Query("sort") String sort);

    /** Get the link to upload file on the disk. */
    @GET("disk/resources/upload")
    Flowable<Link> getUploadLink(@Query("path") String path,
                                 @Query("overwrite") boolean overwrite);

    /** Upload file on the disk. */
    @Multipart
    @PUT
    Completable uploadImage(@Url String url,
                            @Part MultipartBody.Part file);

    /** Download file from the disk folder. */
    @GET("disk/resources/download")
    Flowable<Link> getDownloadLink(@Query("path") String path);

    /** Delete file from the disk folder. */
    @DELETE("disk/resources")
    Completable deleteImage(@Query("path") String path);

    /** Get files from the trash folder. */
    @GET("disk/trash/resources")
    Flowable<Trash> getTrashFiles(@Query("path") String path,
                                  @Query("limit") int limit,
                                  @Query("offset") int offset,
                                  @Query("preview_size") String size,
                                  @Query("sort") String sort);

    /** Restore file from the trash folder. */
    @PUT("disk/trash/resources/restore")
    Completable restorePhoto(@Query("path") String path);

    /**
     * Clear trash folder if {@code path} null,
     * otherwise delete the file by this {@code path}.
     */
    @DELETE("disk/trash/resources")
    Completable deletePermanently(@Query("path") @Nullable String path);
}
