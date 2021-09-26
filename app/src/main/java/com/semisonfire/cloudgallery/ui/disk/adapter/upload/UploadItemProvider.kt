package com.semisonfire.cloudgallery.ui.disk.adapter.upload

import android.view.View
import com.semisonfire.cloudgallery.R.layout
import com.semisonfire.cloudgallery.adapter.factory.ItemProvider
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import javax.inject.Inject

class UploadItemProvider @Inject constructor() : ItemProvider() {
    override fun provideViewHolder(view: View, viewType: Int): ItemViewHolder<out Item> {
        return UploadItemViewHolder(view)
    }

    override fun provideItemLayout(viewType: Int): Int? {
        return layout.item_upload_photo
    }

    override fun checkItemViewType(item: Item): Boolean {
        return item is UploadItem
    }

}