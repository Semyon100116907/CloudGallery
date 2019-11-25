package com.semisonfire.cloudgallery.data.remote.response

import android.arch.persistence.room.Ignore
import com.google.gson.annotations.SerializedName
import com.semisonfire.cloudgallery.data.model.Photo

class Link {
  @SerializedName("href")
  val href: String? = null
  @Ignore
  var photo: Photo = Photo()
}