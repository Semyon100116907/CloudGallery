package com.semisonfire.cloudgallery.ui.disk.model.remote

import com.google.gson.annotations.SerializedName
import com.semisonfire.cloudgallery.core.data.model.Photo

class DiskResponse {
  @SerializedName("items")
  var photos: List<Photo>? = null
}