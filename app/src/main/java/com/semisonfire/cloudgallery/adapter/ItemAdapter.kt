package com.semisonfire.cloudgallery.adapter

import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactory
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactoryImpl
import com.semisonfire.cloudgallery.adapter.factory.ItemProvider
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import java.util.*

@Suppress("UNCHECKED_CAST")
abstract class ItemAdapter<I : Item>(
    private val factory: AdapterFactory
) : RecyclerView.Adapter<ItemViewHolder<out I>>() {

    constructor(providers: Set<ItemProvider>) : this(AdapterFactoryImpl(providers))

    protected val items = mutableListOf<I>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder<out I> {
        return factory.create(parent, viewType) as ItemViewHolder<I>
    }

    override fun getItemViewType(position: Int): Int {
        return factory.getItemViewType(items, position)
    }

    @CallSuper
    override fun onBindViewHolder(viewHolder: ItemViewHolder<out I>, position: Int) {
        (viewHolder as ItemViewHolder<in I>).bind(items[position])
    }

    override fun onViewAttachedToWindow(holder: ItemViewHolder<out I>) {
        super.onViewAttachedToWindow(holder)
        holder.attach()
    }

    override fun onViewDetachedFromWindow(holder: ItemViewHolder<out I>) {
        super.onViewDetachedFromWindow(holder)
        holder.detach()
    }

    override fun onViewRecycled(holder: ItemViewHolder<out I>) {
        super.onViewRecycled(holder)
        holder.detach()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getItem(position: Int): I? {
        return items.getOrNull(position)
    }

    fun getItemsList(): List<I> {
        return Collections.unmodifiableList(items)
    }

    open fun updateDataSet(newItems: List<I>) {
        val diff =
            DiffUtil.calculateDiff(ItemDiffCallback(items, newItems))

        items.clear()
        items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    fun addItem(item: I) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun addItem(item: I, position: Int) {
        if (!isInBounds(position)) return

        items.add(position, item)
        notifyItemInserted(position)
    }

    fun addItems(newItems: List<I>) {
        if (newItems.isEmpty()) return

        val position = itemCount - 1
        addItems(newItems, position)
    }

    fun addItems(newItems: List<I>, position: Int) {
        if (newItems.isEmpty()) return

        if (position < 0) return

        if (position < itemCount) {
            items.addAll(position, newItems)
        } else {
            items.addAll(newItems)
        }
        notifyItemRangeInserted(position, newItems.size)
    }

    fun updateItem(item: I) {
        val position = items.indexOf(item)
        updateItem(item, position)
    }

    fun updateItem(item: I, position: Int) {
        if (!isInBounds(position)) return

        items[position] = item
        notifyItemChanged(position)
    }

    fun updateItems(updateItems: List<I>) {
        if (updateItems.isEmpty()) return

        var i = 0
        while (i < updateItems.size) {
            val newItem = updateItems[i]
            val position = items.indexOf(newItem)
            if (isInBounds(position)) {
                items[position] = newItem
                notifyItemChanged(position)
            }
            i++
        }
    }

    fun removeItem(item: I) {
        val position = items.indexOf(item)
        removeItemByPosition(position)
    }

    fun removeItemByPosition(position: Int) {
        if (!isInBounds(position)) return

        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeItems(removeItems: List<I>) {
        if (removeItems.isEmpty()) return

        var i = 0
        while (i < removeItems.size) {
            val newItem = removeItems[i]
            val position = items.indexOf(newItem)
            if (isInBounds(position)) {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
            i++
        }
    }

    private fun isInBounds(position: Int) = position in 0..itemCount

    private class ItemDiffCallback<I : Item>(
        private val items: List<I>,
        private val newItems: List<I>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = items.size
        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return items[oldItemPosition].areItemsTheSame(newItems[newItemPosition])
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return items[oldItemPosition].areContentTheSame(newItems[newItemPosition])
        }
    }
}