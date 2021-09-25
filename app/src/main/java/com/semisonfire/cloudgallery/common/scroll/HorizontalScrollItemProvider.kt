package com.semisonfire.cloudgallery.common.scroll

import android.view.View
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.adapter.factory.ItemProvider
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import javax.inject.Inject

class HorizontalScrollItemProvider @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards ItemProvider>
) : ItemProvider() {

    override fun provideViewHolder(view: View, viewType: Int): ItemViewHolder<out Item> {
        return HorizontalViewHolder(view, providers)
    }

    override fun provideItemLayout(viewType: Int): Int {
        return R.layout.item_scroll
    }

    override fun checkItemViewType(item: Item): Boolean {
        return item is HorizontalScrollItem<out Item>
    }
}