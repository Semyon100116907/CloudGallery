package com.semisonfire.cloudgallery.ui.disk

import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView

interface DiskContract {

  interface View : MvpView {

    fun onPhotosLoaded(photos: List<Photo>)

    fun onUploadingPhotos(photos: List<Photo>)

    fun onPhotoUploaded(photo: Photo)

    fun onPhotoDownloaded(path: String)

    fun onPhotoDeleted(photo: Photo)
  }

  interface Presenter : MvpPresenter<View> {

    fun getPhotos(offset: Int)

    fun uploadPhotos(photos: List<Photo>)

    fun uploadPhoto(photo: Photo)
    fun getUploadingPhotos()
    fun downloadPhotos(photos: List<Photo>)
    fun deletePhotos(photos: List<Photo>)

  }

}
