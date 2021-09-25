package com.semisonfire.cloudgallery.common.scroll

import com.semisonfire.cloudgallery.adapter.holder.Item

data class HorizontalScrollItem<I : Item>(
    val id: Long,
    val items: List<I>
) : Item {

    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is HorizontalScrollItem<out Item>) return false

        return this.id == item.id
    }

    override fun areContentTheSame(item: Item): Boolean {
        if (item !is HorizontalScrollItem<out Item>) return false

        return this == item
    }
}
