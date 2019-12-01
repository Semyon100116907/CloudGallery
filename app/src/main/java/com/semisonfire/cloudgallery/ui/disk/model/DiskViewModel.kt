package com.semisonfire.cloudgallery.ui.disk.model

import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.mvp.MvpViewModel
import java.util.concurrent.atomic.AtomicInteger

data class DiskViewModel(
  val photoList: MutableList<Photo> = mutableListOf(),
  val selectedPhotoList: MutableMap<String, Photo> = mutableMapOf()
) : MvpViewModel {

  val currentPage = AtomicInteger(0)
  var selectionMode: Boolean = false
}