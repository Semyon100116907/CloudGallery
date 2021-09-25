package com.semisonfire.cloudgallery.ui.disk

import com.semisonfire.cloudgallery.core.data.model.Photo

sealed class DiskResult {

    data class Loaded(val photos: List<Photo>) : DiskResult()
    data class Uploading(val photos: List<Photo>) : DiskResult()
    data class PhotoUploaded(val photo: Photo, val uploaded: Boolean) : DiskResult()
    data class PhotoDownloaded(val path: String) : DiskResult()
    data class PhotoDeleted(val photo: Photo) : DiskResult()

    data class LoadMoreCompleted(val photos: List<Photo>) : DiskResult()
}