package com.semisonfire.cloudgallery.ui.custom

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ItemDecoration
import android.view.View
import com.semisonfire.cloudgallery.ui.disk.adapter.items.TYPE_HEADER

class ItemDecorator(private val space: Int) : ItemDecoration() {

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    super.getItemOffsets(outRect, view, parent, state)
    val layoutManager = parent.layoutManager
    val position = parent.getChildAdapterPosition(view)

    when (layoutManager) {
      is GridLayoutManager -> setGridSpace(outRect, layoutManager, position)
      is LinearLayoutManager -> setLinearSpace(outRect, layoutManager, parent.adapter, position)
    }
  }

  private fun setGridSpace(
    outRect: Rect,
    gridLayoutManager: GridLayoutManager,
    position: Int
  ) {
    val spans = gridLayoutManager.spanCount
    val rows = position / spans
    outRect.left = space
    outRect.right = if (position % spans == spans - 1) space else 0
    outRect.top = space
    outRect.bottom = if (position / spans == rows - 1) space else 0
  }

  private fun setLinearSpace(
    outRect: Rect,
    linearLayoutManager: LinearLayoutManager,
    adapter: RecyclerView.Adapter<*>?,
    position: Int
  ) {
    if (adapter == null) return

    if (linearLayoutManager.canScrollHorizontally()) {
      outRect.left = space
      outRect.right = if (position == adapter.itemCount - 1) space else 0
    } else {
      outRect.top = if (position == 0) space else 0
      outRect.bottom = space

      when {
        position == -1 -> outRect.bottom = 0
        adapter.getItemViewType(position) == TYPE_HEADER -> outRect.bottom = 0
      }
    }
  }

}