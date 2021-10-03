package com.semisonfire.cloudgallery.upload

import com.semisonfire.cloudgallery.data.model.Photo

sealed class UploadResult(open val photo: Photo, val uploaded: Boolean) {
    data class Success(override val photo: Photo) : UploadResult(photo, true)
    data class Fail(override val photo: Photo) : UploadResult(photo, false)
}