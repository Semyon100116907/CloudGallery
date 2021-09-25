package com.semisonfire.cloudgallery.common.photo

import android.view.View
import com.semisonfire.cloudgallery.R.layout
import com.semisonfire.cloudgallery.adapter.factory.ItemProvider
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import javax.inject.Inject

class PhotoItemProvider @Inject constructor() : ItemProvider() {

    override fun provideViewHolder(view: View, viewType: Int): ItemViewHolder<out Item> {
        return PhotoViewHolder(view)
    }

    override fun provideItemLayout(viewType: Int): Int {
        return layout.item_photo
    }

    override fun checkItemViewType(item: Item): Boolean {
        return item is PhotoItem
    }
}