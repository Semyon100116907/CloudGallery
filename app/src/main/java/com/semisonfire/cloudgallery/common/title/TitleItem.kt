package com.semisonfire.cloudgallery.common.title

import com.semisonfire.cloudgallery.adapter.holder.Item

data class TitleItem(
    val title: String,
    val subtitle: String
) : Item {
    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is TitleItem) return false

        return this === item
    }

    override fun areContentTheSame(item: Item): Boolean {
        if (item !is TitleItem) return false

        return this == item
    }
}