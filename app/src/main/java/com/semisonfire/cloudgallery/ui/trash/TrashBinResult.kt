package com.semisonfire.cloudgallery.ui.trash

import com.semisonfire.cloudgallery.data.model.Photo

sealed class TrashBinResult {

    data class Loaded(val photos: List<Photo>) : TrashBinResult()
    data class PhotoDeleted(val photo: Photo) : TrashBinResult()
    data class PhotoRestored(val photo: Photo) : TrashBinResult()
    object Cleared : TrashBinResult()
}