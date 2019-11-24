package com.semisonfire.cloudgallery.data.local

import com.semisonfire.cloudgallery.data.model.Photo
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(private val localDatabase: LocalDatabase) {

  val uploadingPhotos: Flowable<List<Photo>>
    get() = localDatabase.photoDao.uploadingPhotos.toFlowable()

  fun saveUploadingPhoto(photo: Photo): Flowable<Photo> {
    photo.isUploaded = false
    return savePhoto(photo)
  }

  fun removeUploadingPhoto(photo: Photo?): Completable {
    return Completable.fromAction {
      localDatabase.photoDao.deletePhoto(photo)
    }
  }

  private fun savePhoto(photo: Photo): Flowable<Photo> {
    return Completable
      .fromCallable {
        photo.id = localDatabase.photoDao.insertPhoto(photo)
        photo
      }
      .subscribeOn(Schedulers.io())
      .andThen(Flowable.just(photo))
  }

}