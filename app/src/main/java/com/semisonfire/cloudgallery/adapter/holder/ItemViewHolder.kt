package com.semisonfire.cloudgallery.adapter.holder

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.semisonfire.cloudgallery.adapter.ItemAdapter

abstract class ItemViewHolder<I : Item>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    protected val context: Context = itemView.context

    @Suppress("UNCHECKED_CAST")
    protected fun getItem(): I? {
        val itemAdapter = bindingAdapter as? ItemAdapter<I>
        return itemAdapter?.getItem(bindingAdapterPosition)
    }

    open fun bind(item: I) {
    }

    open fun attach() {}
    open fun detach() {}
}