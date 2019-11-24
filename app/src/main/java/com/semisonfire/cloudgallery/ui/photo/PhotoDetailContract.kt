package com.semisonfire.cloudgallery.ui.photo

import android.graphics.Bitmap
import android.net.Uri

import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView

interface PhotoDetailContract {

  interface View : MvpView {

    fun onPhotoDownloaded(path: String)

    fun onFilePrepared(uri: Uri)

    fun onFilesChanged(photo: Photo)
  }

  interface Presenter : MvpPresenter<View> {

    fun download(photo: Photo)

    fun delete(photo: Photo, from: Int)

    fun restore(photo: Photo)

    fun createShareFile(bitmap: Bitmap)
  }
}
