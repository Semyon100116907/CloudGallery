package com.semisonfire.cloudgallery.common.title

import android.view.View
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import com.semisonfire.cloudgallery.databinding.ItemTitleBinding

class TitleItemViewHolder(view: View) : ItemViewHolder<TitleItem>(view) {

    private val viewBinding = ItemTitleBinding.bind(view)

    override fun bind(item: TitleItem) {
        super.bind(item)
        viewBinding.textTitle.text = item.title
        viewBinding.textSubtitle.text = item.subtitle
    }
}