package com.semisonfire.cloudgallery.ui.disk.model.remote

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import com.semisonfire.cloudgallery.core.data.model.Photo

class Link {
    @SerializedName("href")
    val href: String? = null

    @Ignore
    var photo: Photo = Photo()
}