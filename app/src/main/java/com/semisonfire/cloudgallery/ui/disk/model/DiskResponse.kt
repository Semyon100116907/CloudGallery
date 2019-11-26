package com.semisonfire.cloudgallery.ui.disk.model

import com.google.gson.annotations.SerializedName
import com.semisonfire.cloudgallery.data.model.Photo

class DiskResponse {
  @SerializedName("items")
  var photos: List<Photo>? = null
}