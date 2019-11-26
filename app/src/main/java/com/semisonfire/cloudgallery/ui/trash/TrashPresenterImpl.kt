package com.semisonfire.cloudgallery.ui.trash

import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.ui.disk.LIMIT
import com.semisonfire.cloudgallery.ui.trash.data.TrashRepository
import com.semisonfire.cloudgallery.utils.background
import com.semisonfire.cloudgallery.utils.foreground
import com.semisonfire.cloudgallery.utils.printThrowable
import io.reactivex.Observable

interface TrashPresenter : MvpPresenter<TrashView> {

  fun getPhotos(page: Int)
  fun restorePhotos(photos: List<Photo>)
  fun deletePhotos(photos: List<Photo>)
  fun clear()
}

class TrashPresenterImpl(
  private val trashRepository: TrashRepository
) : BasePresenter<TrashView>(), TrashPresenter {

  override fun getPhotos(page: Int) {
    compositeDisposable.add(
      trashRepository.getTrashPhotos(LIMIT, page)
        .map { photoList ->
          photoList.asSequence()
            .filter { it.mediaType.isNotEmpty() }
            .filter { it.mediaType == "image" }
            .toList()
        }
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onTrashLoaded(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun restorePhotos(photos: List<Photo>) {
    compositeDisposable.add(
      Observable.fromIterable(photos.toMutableList())
        .concatMap { trashRepository.restoreTrashPhoto(it) }
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onPhotoRestored(it) },
          { it.printThrowable() }
        )
    )
  }

  override fun deletePhotos(photos: List<Photo>) {
    compositeDisposable.add(
      Observable.fromIterable(photos.toMutableList())
        .concatMap { trashRepository.deleteTrashPhoto(it) }
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
      trashRepository.clearTrash()
        .subscribeOn(background())
        .observeOn(foreground())
        .subscribe(
          { view?.onTrashCleared() },
          { it.printThrowable() }
        )
    )
  }
}