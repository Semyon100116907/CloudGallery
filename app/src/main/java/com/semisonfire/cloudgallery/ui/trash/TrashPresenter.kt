package com.semisonfire.cloudgallery.ui.trash

import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.RemoteRepository
import com.semisonfire.cloudgallery.ui.disk.LIMIT
import com.semisonfire.cloudgallery.utils.background
import com.semisonfire.cloudgallery.utils.foreground
import com.semisonfire.cloudgallery.utils.printThrowable
import io.reactivex.Flowable
import java.util.*

class TrashPresenter(private val remoteRepository: RemoteRepository) :
  BasePresenter<TrashContract.View>(), TrashContract.Presenter {

  override fun getPhotos(page: Int) {
    compositeDisposable.add(
      remoteRepository.getTrashPhotos(LIMIT, page)
        .flatMapIterable { it }
        .filter { it.mediaType.isNotEmpty() }
        .filter { it.mediaType == "image" }
        .toList()
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onTrashLoaded(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun restorePhotos(photos: List<Photo>) {
    val items: List<Photo> = ArrayList(photos)
    compositeDisposable.add(
      Flowable.fromIterable(items)
        .concatMap { remoteRepository.restoreTrashPhoto(it) }
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onPhotoRestored(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun deletePhotos(photos: List<Photo>) {
    val items: List<Photo> = ArrayList(photos)
    compositeDisposable.add(
      Flowable.fromIterable(items)
        .concatMap { remoteRepository.deleteTrashPhoto(it) }
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onPhotoDeleted(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun clear() {
    compositeDisposable.add(
      remoteRepository.clearTrash()
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onTrashCleared() },
          { it.printThrowable() }
        )
    )
  }
}