package com.semisonfire.cloudgallery.ui.disk.data

import com.semisonfire.cloudgallery.common.photo.PhotoItem
import com.semisonfire.cloudgallery.common.scroll.HorizontalScrollItem
import com.semisonfire.cloudgallery.common.title.TitleItem

data class DiskValue(
    val titleItem: TitleItem,
    val scrollItem: HorizontalScrollItem<PhotoItem>
)