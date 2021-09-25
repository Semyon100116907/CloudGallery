package com.semisonfire.cloudgallery.adapter.factory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder

abstract class ItemProvider {

    abstract fun provideViewHolder(
        view: View,
        viewType: Int
    ): ItemViewHolder<out Item>

    protected abstract fun provideItemLayout(viewType: Int): Int?

    fun createView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): View {
        val layout = provideItemLayout(viewType)
            ?: throw IllegalArgumentException("Adapter item layout not found")

        return inflater.inflate(layout, parent, false)
    }

    abstract fun checkItemViewType(item: Item): Boolean
}