package com.semisonfire.cloudgallery.ui.trash.data

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.common.photo.PhotoItem
import com.semisonfire.cloudgallery.data.model.Photo
import javax.inject.Inject

class TrashBinMapper @Inject constructor() {

    fun map(photos: List<Photo>): List<Item> {
        return photos
            .asSequence()
            .map { PhotoItem(it.id, it.name, it.preview) }
            .toList()
    }
}