package com.semisonfire.cloudgallery.common.scroll

import android.view.View
import com.semisonfire.cloudgallery.adapter.ItemAdapter
import com.semisonfire.cloudgallery.adapter.decoration.SpaceItemDecorator
import com.semisonfire.cloudgallery.adapter.factory.ItemProvider
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import com.semisonfire.cloudgallery.databinding.ItemScrollBinding

class HorizontalViewHolder(view: View, providers: Set<ItemProvider>) :
    ItemViewHolder<HorizontalScrollItem<out Item>>(view) {

    private val adapter = HorizontalAdapter(providers)
    private val viewBinding = ItemScrollBinding.bind(view)

    init {
        viewBinding.rvHorizontalScroll.itemAnimator = null
        viewBinding.rvHorizontalScroll.adapter = adapter
        viewBinding.rvHorizontalScroll.addItemDecoration(SpaceItemDecorator())
    }

    override fun bind(item: HorizontalScrollItem<out Item>) {
        super.bind(item)
        adapter.updateDataSet(item.items)
    }

    class HorizontalAdapter(providers: Set<ItemProvider>) : ItemAdapter<Item>(providers)
}