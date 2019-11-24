package com.semisonfire.cloudgallery.ui.disk

import android.graphics.BitmapFactory
import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.data.local.LocalRepository
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.RemoteRepository
import com.semisonfire.cloudgallery.data.remote.exceptions.InternetUnavailableException
import com.semisonfire.cloudgallery.utils.DateUtils
import com.semisonfire.cloudgallery.utils.FileUtils
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit


const val LIMIT = 15

class DiskPresenter(
  private val remoteRepository: RemoteRepository,
  private val localRepository: LocalRepository
) : BasePresenter<DiskContract.View>(), DiskContract.Presenter {

  //Upload
  private val uploadSubject = PublishSubject.create<Photo>()
  private val uploadSubjectInformer = PublishSubject.create<Photo>()
  private val uploadingPhotos: Queue<Photo> = LinkedList()

  init {
    createInformerSubject()
    createUploadSubj()
  }

  override fun getPhotos(offset: Int) {
    val result = remoteRepository.getPhotos(LIMIT, offset)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        { view?.onPhotosLoaded(it) },
        { view?.onError(it) }
      )

    compositeDisposable.add(result)
  }

  override fun getUploadingPhotos() {
    compositeDisposable.add(
      localRepository.uploadingPhotos
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          { view?.onUploadingPhotos(it) },
          { view?.onError(it) }
        )
    )
  }

  override fun downloadPhotos(photos: List<Photo>) {
    val items: List<Photo> = ArrayList(photos)
    compositeDisposable.add(
      Flowable.fromIterable(items)
        .concatMap { remoteRepository.getDownloadLink(it) }
        .map {
          val url = URL(it.href)
          val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
          FileUtils.getInstance().savePublicFile(bitmap, it.photo.name)
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          { view?.onPhotoDownloaded(it) },
          { view?.onError(it) }
        )
    )
  }

  override fun deletePhotos(photos: List<Photo>) {
    val items: List<Photo> = ArrayList(photos)

    compositeDisposable.add(
      Flowable.fromIterable(items)
        .concatMap { remoteRepository.deletePhoto(it) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          { view?.onPhotoDeleted(it) },
          { view?.onError(it) }
        )
    )
  }

  override fun uploadPhoto(photo: Photo) {
    uploadingPhotos.add(photo)
    uploadNext()
  }

  override fun uploadPhotos(photos: List<Photo>) {
    uploadingPhotos.addAll(photos)
    uploadNext()
  }

  private fun uploadNext() {
    while (!uploadingPhotos.isEmpty()) {
      uploadSubject.onNext(uploadingPhotos.remove())
    }
  }

  /**
   * Subject which inform android main thread about error
   */
  private fun createInformerSubject() {
    compositeDisposable.add(
      uploadSubjectInformer
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          { view?.onPhotoUploaded(it, false) },
          { view?.onError(it) }
        )
    )
  }

  private fun createUploadSubj() {
    compositeDisposable.add(
      uploadSubject
        .flatMap {
          localRepository.saveUploadingPhoto(it).toObservable()
        }
        .concatMap { photo: Photo -> getUploadFlowable(photo).toObservable() }
        .subscribe(
          { view?.onPhotoUploaded(it, true) },
          { view?.onError(it) }
        )
    )
  }

  /**
   * Upload one photo at the time.
   */
  private fun getUploadFlowable(photo: Photo): Flowable<Photo> {
    return Flowable.just(photo)
      .subscribeOn(Schedulers.io())
      .switchMap {
        Flowable.just(it).delay(1000, TimeUnit.MILLISECONDS)
      }
      .flatMap { remoteRepository.getUploadLink(it) }
      .flatMap { remoteRepository.savePhoto(it.photo, it) }
      .retryWhen { error: Flowable<Throwable?> ->
        error.flatMap { throwable: Throwable? ->
          if (throwable is InternetUnavailableException) {
            uploadSubjectInformer.onNext(Photo())
            return@flatMap Flowable.timer(2, TimeUnit.SECONDS)
          }
          if (throwable is HttpException) {
            if (throwable.code() == 409) {
              photo.name = rename(photo.name)
              return@flatMap Flowable.timer(1, TimeUnit.SECONDS)
            }
          }
          Flowable.error<Long?>(throwable)
        }
      }
      .filter { it.isUploaded }
      .flatMap {
        val absolutePath = "file://" + File(it.localPath).absolutePath
        it.preview = absolutePath
        it.modifiedAt = DateUtils.getCurrentDate()
        localRepository.removeUploadingPhoto(it)
          .andThen(Flowable.just(it))
      }
      .observeOn(AndroidSchedulers.mainThread())
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