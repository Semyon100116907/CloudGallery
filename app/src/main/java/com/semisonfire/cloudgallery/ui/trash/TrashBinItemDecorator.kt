package com.semisonfire.cloudgallery.ui.trash

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class TrashBinItemDecorator(private val space: Int) : ItemDecoration() {

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
            is LinearLayoutManager -> setLinearSpace(
                outRect,
                layoutManager,
                parent.adapter,
                position
            )
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

            if (position == -1) outRect.bottom = 0
        }
    }

}