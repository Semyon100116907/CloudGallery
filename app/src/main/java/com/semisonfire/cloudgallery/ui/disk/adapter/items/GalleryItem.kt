package com.semisonfire.cloudgallery.ui.disk.adapter.items

import com.semisonfire.cloudgallery.data.model.Photo
import java.util.ArrayList

class GalleryItem : DiskItem() {
    var photos: List<Photo> = ArrayList()

    fun getMutablePhotos(): MutableList<Photo> {
        return photos as MutableList<Photo>
    }

    override val type: Int
        get() = TYPE_GALLERY
}