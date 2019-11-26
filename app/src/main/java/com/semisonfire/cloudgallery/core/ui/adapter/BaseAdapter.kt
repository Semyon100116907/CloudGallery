package com.semisonfire.cloudgallery.core.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

interface ItemClickListener<T> {
    fun onItemClick(item: T)
    fun onItemLongClick(item: T)
}

abstract class BaseAdapter<T, VH : BaseViewHolder<T>> : RecyclerView.Adapter<VH>() {

    protected val items = mutableListOf<T>()
    private var itemClickListener: ItemClickListener<T>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(layoutId(), parent, false)
        val viewHolder = createViewHolder(view)
        createItemListeners(view, viewHolder)
        return viewHolder
    }

    protected open fun createItemListeners(view: View, viewHolder: VH) {
        view.setOnClickListener {
            val item = getItem(viewHolder)
              ?: return@setOnClickListener

            itemClickListener?.onItemClick(item)
        }
        view.setOnLongClickListener {
            val item = getItem(viewHolder)
              ?: return@setOnLongClickListener false

            itemClickListener?.onItemLongClick(item)
            true
        }
    }

    protected open fun getItem(viewHolder: VH): T? {
        val position = viewHolder.adapterPosition
        if (position == RecyclerView.NO_POSITION) return null

        return items[position]
    }

    override fun onBindViewHolder(viewHolder: VH, position: Int) {
        val item = items[position]
        viewHolder.bindItem(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    abstract fun layoutId(): Int

    abstract fun createViewHolder(view: View): VH

    fun getItemsList(): List<T> {
        return Collections.unmodifiableList(items)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener<T>) {
        this.itemClickListener = itemClickListener
    }

    fun getItemClickListener(): ItemClickListener<T>? {
        return itemClickListener
    }

    open fun updateDataSet(newItems: List<T>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: T) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun addItem(item: T, position: Int) {
        if (!isInBounds(position))
            return

        items.add(position, item)
        notifyItemInserted(position)
    }

    fun addItems(newItems: List<T>) {
        if (newItems.isEmpty())
            return

        val position = itemCount - 1
        addItems(newItems, position)
    }

    fun addItems(newItems: List<T>, position: Int) {
        if (newItems.isEmpty())
            return

        if (position < 0)
            return

        if (position < itemCount) {
            items.addAll(position, newItems)
        } else {
            items.addAll(newItems)
        }
        notifyItemRangeInserted(position, newItems.size)
    }

    fun updateItem(item: T) {
        val position = items.indexOf(item)
        updateItem(item, position)
    }

    fun updateItem(item: T, position: Int) {
        if (!isInBounds(position))
            return

        items[position] = item
        notifyItemChanged(position)
    }

    fun updateItems(updateItems: List<T>) {
        if (updateItems.isEmpty())
            return

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

    fun removeItem(item: T) {
        val position = items.indexOf(item)
        removeItemByPosition(position)
    }

    fun removeItemByPosition(position: Int) {
        if (!isInBounds(position))
            return
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeItems(removeItems: List<T>) {
        if (removeItems.isEmpty())
            return

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

    private fun isInBounds(position: Int) = position < itemCount && position > -1
}