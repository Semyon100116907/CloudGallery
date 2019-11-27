package com.semisonfire.cloudgallery.core.data.remote.api

import com.semisonfire.cloudgallery.ui.disk.model.remote.DiskResponse
import com.semisonfire.cloudgallery.ui.disk.model.remote.Link
import com.semisonfire.cloudgallery.ui.trash.model.remote.Trash
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*

interface DiskApi {
  /** Get images from all disk folders besides trash folder.  */
  @GET("disk/resources/files")
  fun getDiskImages(
    @Query("limit") limit: Int,
    @Query("offset") offset: Int,
    @Query("media_type") mediaType: String?,
    @Query("preview_size") size: String?,
    @Query("sort") sort: String?
  ): Single<DiskResponse>

  /** Get the link to upload file on the disk.  */
  @GET("disk/resources/upload")
  fun getUploadLink(
    @Query("path") path: String?,
    @Query("overwrite") overwrite: Boolean
  ): Observable<Link>

  /** Upload file on the disk.  */
  @Multipart
  @PUT
  fun uploadImage(
    @Url url: String?,
    @Part file: MultipartBody.Part?
  ): Completable

  /** Download file from the disk folder.  */
  @GET("disk/resources/download")
  fun getDownloadLink(@Query("path") path: String?): Observable<Link>

  /** Delete file from the disk folder.  */
  @DELETE("disk/resources")
  fun deleteImage(@Query("path") path: String?): Completable

  /** Get files from the trash folder.  */
  @GET("disk/trash/resources")
  fun getTrashFiles(
    @Query("path") path: String?,
    @Query("limit") limit: Int,
    @Query("offset") offset: Int,
    @Query("preview_size") size: String?,
    @Query("sort") sort: String?
  ): Single<Trash>

  /** Restore file from the trash folder.  */
  @PUT("disk/trash/resources/restore")
  fun restorePhoto(@Query("path") path: String?): Completable

  /**
   * Clear trash folder if `path` null,
   * otherwise delete the file by this `path`.
   */
  @DELETE("disk/trash/resources")
  fun deletePermanently(@Query("path") path: String?): Completable
}