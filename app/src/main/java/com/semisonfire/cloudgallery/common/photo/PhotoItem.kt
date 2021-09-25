package com.semisonfire.cloudgallery.common.photo

import com.semisonfire.cloudgallery.adapter.holder.Item

data class PhotoItem(
    val id: String,
    val name: String,
    val url: String
) : Item {
    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is PhotoItem) return false

        return id == item.id
    }

    override fun areContentTheSame(item: Item): Boolean {
        if (item !is PhotoItem) return false

        return this == item
    }
}