package com.semisonfire.cloudgallery.ui.trash

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.data.model.Photo

sealed class TrashBinResult {

    data class Loaded(val photos: List<Item>) : TrashBinResult()
    data class PhotoDeleted(val photo: Photo) : TrashBinResult()
    data class PhotoRestored(val photo: Photo) : TrashBinResult()
    object Cleared : TrashBinResult()
}