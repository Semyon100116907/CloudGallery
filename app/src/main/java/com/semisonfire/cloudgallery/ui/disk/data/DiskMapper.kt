package com.semisonfire.cloudgallery.ui.disk.data

import com.semisonfire.cloudgallery.common.photo.PhotoItem
import com.semisonfire.cloudgallery.common.scroll.HorizontalScrollItem
import com.semisonfire.cloudgallery.common.title.TitleItem
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.utils.DateUtils
import javax.inject.Inject

class DiskMapper @Inject constructor() {

    private var id = 0L

    fun map(
        photos: List<Photo>,
        page: Int
    ): MutableMap<TitleItem, HorizontalScrollItem<PhotoItem>> {

        if (page == 0) id = 0

        val photosByDateMap = mutableMapOf<String, MutableList<PhotoItem>>()
        for (photo in photos) {
            val date = DateUtils.getDateString(photo.modifiedAt, DateUtils.DATE_FORMAT)
                ?: continue

            photosByDateMap
                .getOrPut(date) { mutableListOf() }
                .add(
                    PhotoItem(
                        id = photo.id,
                        name = photo.name,
                        url = photo.preview
                    )
                )
        }

        val result = mutableMapOf<TitleItem, HorizontalScrollItem<PhotoItem>>()
        for ((date, photoItems) in photosByDateMap) {
            val titleItem = TitleItem(
                id = date.hashCode().toLong(),
                title = date,
                subtitle = "${photoItems.size} photo"
            )
            val horizontalScrollItem = HorizontalScrollItem(
                id = date.hashCode().toLong(),
                items = photoItems
            )

            result[titleItem] = horizontalScrollItem
        }

        return result
    }
}