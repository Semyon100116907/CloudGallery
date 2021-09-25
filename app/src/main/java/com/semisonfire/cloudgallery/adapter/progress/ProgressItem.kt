package com.semisonfire.cloudgallery.adapter.progress

import android.view.View
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder

class ProgressItem : Item {
    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is ProgressItem) return false

        return this === item
    }

    override fun areContentTheSame(item: Item): Boolean {
        if (item !is ProgressItem) return false

        return this == item
    }
}

class ProgressViewHolder(view: View) : ItemViewHolder<ProgressItem>(view)