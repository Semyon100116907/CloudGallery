package com.semisonfire.cloudgallery.common.title

import android.view.View
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.adapter.factory.ItemProvider
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import javax.inject.Inject

class TitleItemProvider @Inject constructor() : ItemProvider() {

    override fun provideViewHolder(view: View, viewType: Int): ItemViewHolder<out Item> {
        return TitleItemViewHolder(view)
    }

    override fun provideItemLayout(viewType: Int): Int {
        return R.layout.item_title
    }

    override fun checkItemViewType(item: Item): Boolean {
        return item is TitleItem
    }
}
