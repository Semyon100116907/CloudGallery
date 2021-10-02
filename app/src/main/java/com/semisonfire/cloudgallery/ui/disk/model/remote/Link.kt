package com.semisonfire.cloudgallery.ui.disk.model.remote

import androidx.room.Ignore
import com.semisonfire.cloudgallery.data.model.Photo

data class Link(
    val href: String? = null
) {
    @Ignore
    var photo: Photo = Photo()
}