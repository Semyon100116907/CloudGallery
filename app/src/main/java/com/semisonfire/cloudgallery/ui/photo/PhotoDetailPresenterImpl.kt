package com.semisonfire.cloudgallery.ui.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.RemoteRepository
import com.semisonfire.cloudgallery.utils.FileUtils
import com.semisonfire.cloudgallery.utils.background
import com.semisonfire.cloudgallery.utils.foreground
import com.semisonfire.cloudgallery.utils.printThrowable
import io.reactivex.Flowable
import io.reactivex.Single
import java.net.URL

interface PhotoDetailPresenter : MvpPresenter<PhotoDetailView> {

  fun download(photo: Photo)
  fun delete(photo: Photo, from: Int)
  fun restore(photo: Photo)
  fun createShareFile(bitmap: Bitmap)
}

class PhotoDetailPresenterImpl(
  private val remoteRepository: RemoteRepository
) : BasePresenter<PhotoDetailView>(), PhotoDetailPresenter {

  override fun download(photo: Photo) {
    compositeDisposable.add(
      remoteRepository.getDownloadLink(photo)
        .map {
          val url = URL(it.href)
          val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
          FileUtils.getInstance().savePublicFile(bitmap, photo.name)
        }
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onPhotoDownloaded(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun delete(photo: Photo, from: Int) {
    val delete = when (from) {
      PhotoDetailActivity.FROM_DISK -> remoteRepository.deletePhoto(photo)
      PhotoDetailActivity.FROM_TRASH -> remoteRepository.deleteTrashPhoto(photo)
      else -> Flowable.just(photo)
    }

    compositeDisposable.add(
      delete.subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onFilesChanged(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun restore(photo: Photo) {
    compositeDisposable.add(
      remoteRepository.restoreTrashPhoto(photo)
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onFilesChanged(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun createShareFile(bitmap: Bitmap) {
    compositeDisposable.add(
      Single.just(bitmap)
        .map { FileUtils.getInstance().createShareFile(it) }
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onFilePrepared(it) },
          { it.printThrowable() }
        )
    )
  }

}