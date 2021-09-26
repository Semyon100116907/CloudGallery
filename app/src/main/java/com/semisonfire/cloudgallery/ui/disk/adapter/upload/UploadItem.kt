package com.semisonfire.cloudgallery.ui.disk.adapter.upload

import com.semisonfire.cloudgallery.adapter.holder.Item

data class UploadItem(
    val id: Long,
    val imagePath: String
) : Item {
    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is UploadItem) return false

        return id == item.id
    }

    override fun areContentTheSame(item: Item): Boolean {
        if (item !is UploadItem) return false

        return this == item
    }
}