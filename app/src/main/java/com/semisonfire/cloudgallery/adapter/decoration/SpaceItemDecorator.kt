package com.semisonfire.cloudgallery.adapter.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.semisonfire.cloudgallery.adapter.ItemAdapter
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.utils.dip

class SpaceItemDecorator : RecyclerView.ItemDecoration() {

    private val margin = 16

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val layoutManager = parent.layoutManager ?: return
        val adapter = parent.adapter as? ItemAdapter<out Item> ?: return

        val rect = when (layoutManager) {
            is GridLayoutManager -> TODO("Grid not implemented")
            is LinearLayoutManager -> {
                layoutLinear(view, parent, layoutManager, adapter)
            }
            else -> return
        }

        outRect.set(rect)
    }

    private fun layoutLinear(
        view: View,
        parent: RecyclerView,
        layoutManager: LinearLayoutManager,
        adapter: ItemAdapter<out Item>
    ): Rect {
        return if (layoutManager.orientation == LinearLayoutManager.HORIZONTAL) {
            layoutHorizontal(view, parent, layoutManager, adapter)
        } else {
            layoutVertical(view, parent, layoutManager, adapter)
        }
    }

    private fun layoutVertical(
        view: View,
        parent: RecyclerView,
        layoutManager: LinearLayoutManager,
        adapter: ItemAdapter<out Item>
    ): Rect {
        val outRect = Rect()
        val position = parent.getChildAdapterPosition(view)

        val itemMargin = view.context.dip(margin)
        outRect.top = itemMargin / 2
        outRect.bottom = itemMargin / 2
        outRect.left = itemMargin / 2
        outRect.right = itemMargin / 2

        when (position) {
            0 -> {
                outRect.top = itemMargin
            }
            adapter.itemCount - 1 -> {
                outRect.bottom = itemMargin
            }
        }

        return outRect
    }

    private fun layoutHorizontal(
        view: View,
        parent: RecyclerView,
        layoutManager: LinearLayoutManager,
        adapter: ItemAdapter<out Item>
    ): Rect {
        val outRect = Rect()
        val position = parent.getChildAdapterPosition(view)

        val itemMargin = view.context.dip(margin)
        outRect.top = itemMargin / 2
        outRect.bottom = itemMargin / 2
        outRect.left = itemMargin / 2
        outRect.right = itemMargin / 2

        when (position) {
            0 -> {
                outRect.left = itemMargin
            }
            adapter.itemCount - 1 -> {
                outRect.right = itemMargin
            }
        }

        return outRect
    }
}