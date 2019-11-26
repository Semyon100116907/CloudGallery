package com.semisonfire.cloudgallery.ui.disk.data

import com.semisonfire.cloudgallery.core.data.local.LocalDatabase
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.core.data.remote.exceptions.InternetUnavailableException
import com.semisonfire.cloudgallery.ui.disk.model.Link
import com.semisonfire.cloudgallery.utils.DateUtils
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository @Inject constructor(
  private val diskApi: DiskApi,
  private val database: LocalDatabase,
  private val diskRepository: DiskRepository
) {

  //Upload
  private val uploadSubject = PublishSubject.create<Photo>()
  private val uploadSubjectInformer = PublishSubject.create<Photo>()
  private val uploadingPhotosQueue: Queue<Photo> = LinkedList()

  fun uploadPhotos(vararg photos: Photo) {
    uploadingPhotosQueue.addAll(photos)

    while (!uploadingPhotosQueue.isEmpty()) {
      uploadSubject.onNext(uploadingPhotosQueue.remove())
    }
  }

  fun uploadCompleteListener() = uploadSubject.hide().switchMap { upload(it) }
  fun uploadFailListener() = uploadSubjectInformer.hide()

  val uploadingPhotos: Single<List<Photo>>
    get() = database.photoDao.uploadingPhotos

  fun getUploadLink(photo: Photo): Observable<Link> {
    return diskApi.getUploadLink("disk:/" + photo.name, false)
      .doOnNext { it.photo = photo }
  }

  fun uploadPhoto(photo: Photo): Observable<Photo> {
    return saveUploadingPhoto(photo)
      .flatMapObservable { upload(it) }
  }

  fun saveUploadingPhoto(photo: Photo): Single<Photo> {
    photo.isUploaded = false
    return Single
      .fromCallable {
        photo.id = database.photoDao.insertPhoto(photo)
        photo
      }
      .subscribeOn(Schedulers.io())
  }

  fun removeUploadingPhoto(photo: Photo): Single<Photo> {
    return Single.fromCallable {
      database.photoDao.deletePhoto(photo)
      photo
    }
  }

  /**
   * Upload one photo at the time.
   */
  private fun upload(photo: Photo): Observable<Photo> {
    return Observable.just(photo)
      .switchMapSingle {
        Single.just(it).delay(1000, TimeUnit.MILLISECONDS)
      }
      .flatMap { getUploadLink(it) }
      .flatMap { diskRepository.savePhoto(it.photo, it) }
      .retryWhen {
        it.flatMap { throwable ->
          if (throwable is InternetUnavailableException) {
            uploadSubjectInformer.onNext(Photo())
            return@flatMap Observable.timer(2, TimeUnit.SECONDS)
          }
          if (throwable is HttpException) {
            if (throwable.code() == 409) {
              photo.name = rename(photo.name)
              return@flatMap Observable.timer(1, TimeUnit.SECONDS)
            }
          }
          Observable.error<Long>(throwable)
        }
      }
      .filter { it.isUploaded }
      .flatMapSingle {
        val absolutePath = "file://" + File(it.localPath).absolutePath
        it.preview = absolutePath
        it.modifiedAt = DateUtils.currentDate
        removeUploadingPhoto(it)
      }
  }

  /**
   * Rename duplicate file.
   */
  private fun rename(fullName: String): String {
    val index = fullName.lastIndexOf('.')
    val name = fullName.substring(0, index)
    val extension = fullName.substring(index)
    val first = name.indexOf("(") + 1
    val last = name.lastIndexOf(")")

    var newName = if (last != -1) {
      var counter = name.substring(first, last).toInt()
      counter++
      name.substring(0, first - 1) + "(" + counter.toString() + ")"
    } else {
      "$name(1)"
    }
    newName += extension
    return newName
  }
}