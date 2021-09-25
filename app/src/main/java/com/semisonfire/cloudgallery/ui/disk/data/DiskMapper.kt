package com.semisonfire.cloudgallery.ui.disk.data

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.common.photo.PhotoItem
import com.semisonfire.cloudgallery.common.scroll.HorizontalScrollItem
import com.semisonfire.cloudgallery.common.title.TitleItem
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.utils.DateUtils
import javax.inject.Inject

class DiskMapper @Inject constructor() {

    private var id = 0L

    fun map(photos: List<Photo>, page: Int): List<Item> {

        if (page == 0) id = 0

        val items = mutableListOf<Item>()

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

        for ((date, photoItems) in photosByDateMap) {
            items.add(
                TitleItem(
                    id = id++,
                    title = date,
                    subtitle = "${photoItems.size} photo"
                )
            )
            items.add(
                HorizontalScrollItem(
                    id = id++,
                    items = photoItems
                )
            )
        }

        return items
    }
}