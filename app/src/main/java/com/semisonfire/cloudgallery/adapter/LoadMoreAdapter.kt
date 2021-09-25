package com.semisonfire.cloudgallery.adapter

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactory
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder

abstract class LoadMoreAdapter<I : Item>(factory: AdapterFactory) : ItemAdapter<I>(factory) {

    private val handler = Handler(Looper.getMainLooper()) { true }

    private var recyclerView: RecyclerView? = null
    private var endlessLoading = false
    var endlessScrollEnabled = false

    var endlessScrollThreshold = 1
        set(value) {
            val spanCount = recyclerView?.let {
                when (val layoutManager = it.layoutManager) {
                    is GridLayoutManager -> layoutManager.spanCount
                    is StaggeredGridLayoutManager -> layoutManager.spanCount
                    else -> 1
                }
            } ?: 1
            field = value * spanCount
        }

    var progressItem: I? = null
        set(value) {
            endlessScrollEnabled = value != null
            field = value
        }

    var loadMoreListener: LoadMoreListener? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder<out I>, position: Int) {
        super.onBindViewHolder(viewHolder, position)
        onLoadMore(position)
    }

    protected open fun onLoadMore(position: Int) {
        if (!endlessScrollEnabled || endlessLoading || getItem(position) == progressItem) return

        if (checkNextThreshold(position)) return

        endlessLoading = true
        handler.post {
            showProgressItem()
            loadMoreListener?.loadMore(position)
        }
    }

    protected open fun checkNextThreshold(position: Int): Boolean {
        val threshold = itemCount - endlessScrollThreshold
        val progressPosition = progressItem?.let { items.indexOf(it) } ?: -1
        return position == progressPosition || position < threshold
    }

    open fun onLoadMoreComplete(newItems: List<I>) {
        endlessLoading = false

        val progressPosition = progressItem?.let { items.indexOf(it) } ?: -1
        hideProgressItem()

        val position = if (progressPosition < 0) 0 else progressPosition
        addItems(newItems, position)

        if (newItems.isEmpty() || !endlessScrollEnabled) {
            noMoreLoad(progressPosition)
        }
    }

    private fun showProgressItem() {
        progressItem?.let {
            if (items.contains(it)) return

            addItem(it, itemCount)
        }
    }

    private fun hideProgressItem() {
        progressItem?.let { removeItem(it) }
    }

    private fun noMoreLoad(positionToNotify: Int) {
        if (positionToNotify >= 0) notifyItemChanged(positionToNotify)

        loadMoreListener?.loadMoreComplete()
    }
}