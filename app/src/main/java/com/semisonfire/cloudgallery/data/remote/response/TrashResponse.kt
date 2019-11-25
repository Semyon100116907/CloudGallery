package com.semisonfire.cloudgallery.data.remote.response

import com.google.gson.annotations.SerializedName
import com.semisonfire.cloudgallery.data.model.Photo

class Trash {
  @SerializedName("_embedded")
  var trashResponse: TrashResponse? = null
}

class TrashResponse {
  @SerializedName("items")
  var photos: List<Photo>? = null
}