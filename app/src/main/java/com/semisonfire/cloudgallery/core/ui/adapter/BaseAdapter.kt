package com.semisonfire.cloudgallery.core.ui.adapter

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

interface ItemClickListener<T> {
  fun onItemClick(item: T)
  fun onItemLongClick(item: T)
}

interface LoadMoreListener {
  fun noMoreLoad()
  fun onLoadMore()
}

abstract class BaseAdapter<T, VH : BaseViewHolder<T>> : RecyclerView.Adapter<VH>() {

  protected val items = mutableListOf<T>()

  protected var recyclerView: RecyclerView? = null

  /* EndlessScroll */
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

  protected var endlessLoading = false
  protected var endlessScrollEnabled = false

  protected var progressItem: T? = null
  private val handler = Handler(Looper.getMainLooper(), Handler.Callback { true })

  var itemClickListener: ItemClickListener<T>? = null
  var loadMoreListener: LoadMoreListener? = null

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    this.recyclerView = recyclerView
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    super.onDetachedFromRecyclerView(recyclerView)
    this.recyclerView = null
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val view = LayoutInflater.from(parent.context).inflate(layoutId(), parent, false)
    val viewHolder = createViewHolder(view)
    createItemListeners(view, viewHolder)
    return viewHolder
  }

  protected open fun createItemListeners(view: View, viewHolder: VH) {
    view.setOnClickListener {
      val item = getViewHolder(viewHolder)
        ?: return@setOnClickListener

      itemClickListener?.onItemClick(item)
    }
    view.setOnLongClickListener {
      val item = getViewHolder(viewHolder)
        ?: return@setOnLongClickListener false

      itemClickListener?.onItemLongClick(item)
      true
    }
  }

  protected open fun getViewHolder(viewHolder: VH): T? {
    val position = viewHolder.adapterPosition
    if (position == RecyclerView.NO_POSITION) return null

    return items[position]
  }

  override fun onBindViewHolder(viewHolder: VH, position: Int) {
    val item = items[position]
    viewHolder.bindItem(item)

    onLoadMore(position)
  }

  protected open fun onLoadMore(position: Int) {
    // Skip everything when loading more is unused OR currently loading
    if (!endlessScrollEnabled || endlessLoading || getItem(position) == progressItem) return

    // Check next loading threshold
    if (checkNextThreshold(position)) return

    // Load more if not loading and inside the threshold
    endlessLoading = true
    // Insertion is in post, as suggested by Android because: java.lang.IllegalStateException:
    // Cannot call notifyItemInserted while RecyclerView is computing a layout or scrolling
    handler.post {
      // Show progressItem if not already shown
      showProgressItem()
      // When the listener is not set, loading more is called upon a user request
      loadMoreListener?.onLoadMore()
    }
  }

  protected open fun checkNextThreshold(position: Int): Boolean {
    val threshold = itemCount - endlessScrollThreshold
    val progressPosition = progressItem?.let { items.indexOf(it) } ?: -1
    return position == progressPosition || position < threshold
  }

  protected open fun onLoadMoreComplete(newItems: List<T>) {
    endlessLoading = false

    val progressPosition = progressItem?.let { items.indexOf(it) } ?: -1
    hideProgressItem()

    // Add any new items
    addItems(newItems, progressPosition)

    // Eventually notify noMoreLoad
    if (newItems.isEmpty() || !endlessScrollEnabled) {
      noMoreLoad(progressPosition)
    }
  }

  /**
   * Called at each loading more.
   */
  private fun showProgressItem() {
    progressItem?.let {
      if (items.contains(it)) return

      addItem(it, itemCount)
    }
  }

  /**
   * Called when loading more should continue.
   */
  private fun hideProgressItem() {
    progressItem?.let { removeItem(it) }
  }

  private fun noMoreLoad(positionToNotify: Int) {
    if (positionToNotify >= 0) notifyItemChanged(positionToNotify)

    loadMoreListener?.noMoreLoad()
  }

  private fun getItem(position: Int): T? {
    return items.getOrNull(position)
  }

  override fun getItemCount(): Int {
    return items.size
  }

  abstract fun layoutId(): Int

  abstract fun createViewHolder(view: View): VH

  fun getItemsList(): List<T> {
    return Collections.unmodifiableList(items)
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
    if (!isInBounds(position)) return

    items.add(position, item)
    notifyItemInserted(position)
  }

  fun addItems(newItems: List<T>) {
    if (newItems.isEmpty()) return

    val position = itemCount - 1
    addItems(newItems, position)
  }

  fun addItems(newItems: List<T>, position: Int) {

    if (newItems.isEmpty()) return
    if (position < 0) return

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

  fun removeItem(item: T) {
    val position = items.indexOf(item)
    removeItemByPosition(position)
  }

  fun removeItemByPosition(position: Int) {
    if (!isInBounds(position)) return

    items.removeAt(position)
    notifyItemRemoved(position)
  }

  fun removeItems(removeItems: List<T>) {
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

  private fun isInBounds(position: Int) = position < itemCount && position > RecyclerView.NO_POSITION
}