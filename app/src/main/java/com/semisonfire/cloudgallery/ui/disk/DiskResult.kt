package com.semisonfire.cloudgallery.ui.disk

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.common.scroll.HorizontalScrollItem
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.ui.disk.adapter.upload.UploadItem

sealed class DiskResult {

    data class Loaded(
        val photos: List<Item>,
        val hasMore: Boolean
    ) : DiskResult()

    data class LoadMoreCompleted(
        val photos: List<Item>,
        val hasMore: Boolean
    ) : DiskResult()

    data class Uploading(val uploading: HorizontalScrollItem<UploadItem>) : DiskResult()

    data class PhotoUploaded(val photo: Photo, val uploaded: Boolean) : DiskResult()
    data class PhotoDownloaded(val path: String) : DiskResult()
    data class PhotoDeleted(val photo: Photo) : DiskResult()
}