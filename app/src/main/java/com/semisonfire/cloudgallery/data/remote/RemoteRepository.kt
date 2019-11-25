package com.semisonfire.cloudgallery.data.remote

import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.data.remote.response.Link
import io.reactivex.Completable
import io.reactivex.Flowable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteRepository @Inject constructor(private val diskApi: DiskApi) {

  fun getPhotos(limit: Int, page: Int): Flowable<List<Photo>> {
    return diskApi.getDiskImages(limit, limit * (page - 1), "image", "XL", "-modified")
      .map { it.photos ?: emptyList() }
  }

  fun deletePhoto(photo: Photo): Flowable<Photo> {
    return diskApi.deleteImage(photo.remotePath)
      .andThen(Flowable.just(photo))
  }

  fun getUploadLink(photo: Photo): Flowable<Link> {
    return diskApi.getUploadLink("disk:/" + photo.name, false)
      .doOnNext { it.photo = photo }
  }

  fun savePhoto(photo: Photo, link: Link): Flowable<Photo> {
    val file = File(photo.localPath)
    val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
    val body = MultipartBody.Part.createFormData(
      "file",
      file.name,
      reqFile
    )
    return diskApi.uploadImage(link.href, body)
      .andThen(
        Flowable
          .just(photo)
          .doOnNext {
            it.isUploaded = true
            it.remotePath = "disk:/" + it.name
          }
      )
  }

  fun getTrashPhotos(limit: Int, page: Int): Flowable<List<Photo>> {
    return diskApi.getTrashFiles("trash:/", limit, limit * (page - 1), "XL", "-deleted")
      .map { it.trashResponse?.photos ?: emptyList() }
  }

  fun getDownloadLink(photo: Photo): Flowable<Link> {
    return diskApi.getDownloadLink(photo.remotePath).doOnNext { it.photo = photo }
  }

  fun restoreTrashPhoto(photo: Photo): Flowable<Photo> {
    return diskApi.restorePhoto(photo.remotePath)
      .andThen(Flowable.just(photo))
  }

  fun deleteTrashPhoto(photo: Photo): Flowable<Photo> {
    return diskApi.deletePermanently(photo.remotePath)
      .andThen(Flowable.just(photo))
  }

  fun clearTrash(): Completable {
    return diskApi.deletePermanently(null)
  }
}