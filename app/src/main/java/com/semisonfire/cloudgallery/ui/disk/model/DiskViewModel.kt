package com.semisonfire.cloudgallery.ui.disk.model

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.core.data.model.Photo
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

data class DiskViewModel(
    val items: List<Item> = mutableListOf(),
    val selectedPhotoList: MutableMap<String, Photo> = mutableMapOf()
) {
    val currentPage = AtomicInteger(0)

    val hasMore = AtomicBoolean(true)

    fun setItems(items: List<Item>) {
        this.items as MutableList<Item>
        this.items.clear()
        this.items.addAll(items)
    }

    fun addItems(items: List<Item>) {
        this.items as MutableList<Item>
        this.items.addAll(items)
    }
}