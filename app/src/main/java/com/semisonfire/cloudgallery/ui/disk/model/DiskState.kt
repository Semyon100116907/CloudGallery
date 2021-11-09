package com.semisonfire.cloudgallery.ui.disk.model

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.common.scroll.HorizontalScrollItem
import com.semisonfire.cloudgallery.ui.disk.data.DiskKey
import com.semisonfire.cloudgallery.ui.disk.data.DiskValue
import com.semisonfire.cloudgallery.upload.adapter.UploadItem
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

data class DiskState(
    var uploading: HorizontalScrollItem<UploadItem>? = null,
) {
    private val items = TreeMap<DiskKey, DiskValue>() { firstKey, secondKey ->
        compareValuesBy(secondKey, firstKey) { it.millis }
    }

    val currentPage = AtomicInteger(0)
    val hasMore = AtomicBoolean(true)

    fun setItems(items: Map<DiskKey, DiskValue>) {
        this.items.clear()
        this.items.putAll(items)
    }

    fun mergeItems(itemsMap: Map<DiskKey, DiskValue>) {
        val iterator = itemsMap.iterator()
        while (iterator.hasNext()) {
            val (key, value) = iterator.next()
            val oldValue = items[key]
            if (oldValue == null) {
                items[key] = value
            } else {
                val mergedItems = oldValue.scrollItem.items + value.scrollItem.items

                items[key] = DiskValue(
                    value.titleItem.copy(subtitle = "${mergedItems.size} photo"),
                    value.scrollItem.copy(items = mergedItems)
                )
            }
        }
    }

    fun getListItems(): List<Item> {
        return items.entries
            .flatMapTo(mutableListOf()) {
                listOf(it.value.titleItem, it.value.scrollItem)
            }
            .apply { uploading?.let { add(0, it) } }
    }
}