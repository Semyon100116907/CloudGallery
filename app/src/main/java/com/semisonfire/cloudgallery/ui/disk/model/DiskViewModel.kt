package com.semisonfire.cloudgallery.ui.disk.model

import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.mvp.MvpViewModel

data class DiskViewModel(
  val photoList: MutableList<Photo> = mutableListOf()
) : MvpViewModel