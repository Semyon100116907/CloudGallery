package com.semisonfire.cloudgallery.ui.disk.model

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.core.data.model.Photo
import java.util.concurrent.atomic.AtomicInteger

data class DiskViewModel(
    val photoList: MutableList<Item> = mutableListOf(),
    val selectedPhotoList: MutableMap<String, Photo> = mutableMapOf()
) {

    val currentPage = AtomicInteger(0)
}