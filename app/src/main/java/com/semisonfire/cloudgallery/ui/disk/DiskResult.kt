package com.semisonfire.cloudgallery.ui.disk

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.common.scroll.HorizontalScrollItem
import com.semisonfire.cloudgallery.upload.adapter.UploadItem

sealed class DiskResult {

    data class Update(
        val photos: List<Item>
    ) : DiskResult()

    data class Loaded(
        val photos: List<Item>,
        val hasMore: Boolean
    ) : DiskResult()

    data class LoadMoreCompleted(
        val photos: List<Item>,
        val hasMore: Boolean
    ) : DiskResult()

    data class Uploading(val uploading: HorizontalScrollItem<UploadItem>) : DiskResult()

    data class PhotoDownloaded(val path: String) : DiskResult()
}