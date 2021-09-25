package com.semisonfire.cloudgallery.adapter.progress

import android.view.View
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.adapter.factory.ItemProvider
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import javax.inject.Inject

class ProgressItemProvider @Inject constructor() : ItemProvider() {
    override fun provideViewHolder(view: View, viewType: Int): ItemViewHolder<out Item> {
        return ProgressViewHolder(view)
    }

    override fun provideItemLayout(viewType: Int): Int {
        return R.layout.item_progress
    }

    override fun checkItemViewType(item: Item): Boolean {
        return item is ProgressItem
    }
}