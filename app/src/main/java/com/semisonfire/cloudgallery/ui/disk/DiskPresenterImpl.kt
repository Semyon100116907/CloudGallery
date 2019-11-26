package com.semisonfire.cloudgallery.ui.disk

import android.graphics.BitmapFactory
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.disk.data.UploadRepository
import com.semisonfire.cloudgallery.utils.FileUtils
import com.semisonfire.cloudgallery.utils.background
import com.semisonfire.cloudgallery.utils.foreground
import com.semisonfire.cloudgallery.utils.printThrowable
import io.reactivex.Observable
import java.net.URL

const val LIMIT = 15

interface DiskPresenter : MvpPresenter<DiskView> {

  fun getPhotos(offset: Int)
  fun uploadPhotos(photos: List<Photo>)
  fun uploadPhoto(photo: Photo)
  fun getUploadingPhotos()
  fun downloadPhotos(photos: List<Photo>)
  fun deletePhotos(photos: List<Photo>)
}

class DiskPresenterImpl(
  private val diskRepository: DiskRepository,
  private val uploadRepository: UploadRepository
) : BasePresenter<DiskView>(), DiskPresenter {

  init {
    createInformerSubject()
    createUploadSubj()
  }

  override fun getPhotos(offset: Int) {
    val result = diskRepository.getPhotos(LIMIT, offset)
      .subscribeOn(background())
      .observeOn(foreground())
      .subscribe(
        { view?.onPhotosLoaded(it) },
        { it.printThrowable() }
      )

    compositeDisposable.add(result)
  }

  override fun getUploadingPhotos() {
    compositeDisposable.add(
      uploadRepository.uploadingPhotos
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onUploadingPhotos(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun downloadPhotos(photos: List<Photo>) {
    compositeDisposable.add(
      Observable.fromIterable(photos.toMutableList())
        .concatMap { diskRepository.getDownloadLink(it) }
        .filter { !it.href.isNullOrEmpty() }
        .map {
          val url = URL(it.href)
          val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
          FileUtils.savePublicFile(bitmap, it.photo.name)
        }
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onPhotoDownloaded(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun deletePhotos(photos: List<Photo>) {
    compositeDisposable.add(
      Observable.fromIterable(photos.toMutableList())
        .concatMap { diskRepository.deletePhoto(it) }
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onPhotoDeleted(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun uploadPhoto(photo: Photo) {
    uploadRepository.uploadPhotos(photo)
  }

  override fun uploadPhotos(photos: List<Photo>) {
    uploadRepository.uploadPhotos(*photos.toTypedArray())
  }

  /**
   * Subject which inform android main thread about error
   */
  private fun createInformerSubject() {
    compositeDisposable.add(
      uploadRepository
        .uploadFailListener()
        .observeOn(foreground())
        .subscribe(
          { view?.onPhotoUploaded(it, false) },
          { it.printThrowable() }
        )
    )
  }

  private fun createUploadSubj() {
    compositeDisposable.add(
      uploadRepository
        .uploadCompleteListener()
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onPhotoUploaded(it, true) },
          { it.printThrowable() }
        )
    )
  }
}