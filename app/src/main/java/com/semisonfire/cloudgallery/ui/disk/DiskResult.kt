package com.semisonfire.cloudgallery.ui.disk

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.core.data.model.Photo

sealed class DiskResult {

    data class Loaded(
        val photos: List<Item>,
        val hasMore: Boolean
    ) : DiskResult()

    data class LoadMoreCompleted(
        val photos: List<Item>,
        val hasMore: Boolean
    ) : DiskResult()

    data class Uploading(val photos: List<Photo>) : DiskResult()
    data class PhotoUploaded(val photo: Photo, val uploaded: Boolean) : DiskResult()
    data class PhotoDownloaded(val path: String) : DiskResult()
    data class PhotoDeleted(val photo: Photo) : DiskResult()
}