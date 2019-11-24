package com.semisonfire.cloudgallery.ui.main.trash

import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView

interface TrashContract {

  interface View : MvpView {

    fun onTrashLoaded(photos: List<Photo>)

    fun onPhotoDeleted(photo: Photo)

    fun onPhotoRestored(photo: Photo)

    fun onTrashCleared()

  }

  interface Presenter : MvpPresenter<View> {

    fun getPhotos(page: Int)

    fun restorePhotos(photos: List<Photo>)

    fun deletePhotos(photos: List<Photo>)

    fun clear()
  }

}
