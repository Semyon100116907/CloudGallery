package com.semisonfire.cloudgallery.ui.disk.model

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.common.photo.PhotoItem
import com.semisonfire.cloudgallery.common.scroll.HorizontalScrollItem
import com.semisonfire.cloudgallery.common.title.TitleItem
import com.semisonfire.cloudgallery.data.model.Photo
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

data class DiskViewModel(
    val items: MutableMap<TitleItem, HorizontalScrollItem<PhotoItem>> = mutableMapOf(),
    val selectedPhotoList: MutableMap<String, Photo> = mutableMapOf()
) {
    val currentPage = AtomicInteger(0)
    val hasMore = AtomicBoolean(true)

    fun setItems(items: Map<TitleItem, HorizontalScrollItem<PhotoItem>>) {
        this.items.clear()
        this.items.putAll(items)
    }

    fun mergeItems(itemsMap: MutableMap<TitleItem, HorizontalScrollItem<PhotoItem>>) {
        val iterator = itemsMap.iterator()
        while (iterator.hasNext()) {
            val (key, value) = iterator.next()
            val oldValue = items[key]
            if (oldValue == null) {
                items[key] = value
            } else {
                val mergedItems = oldValue.items + value.items

                items.remove(key)
                items[key.copy(subtitle = "${mergedItems.size} photo")] =
                    oldValue.copy(items = mergedItems)
                iterator.remove()
            }
        }
    }

    fun getListItems(): List<Item> {
        return items.entries.flatMap { listOf(it.key, it.value) }
    }
}