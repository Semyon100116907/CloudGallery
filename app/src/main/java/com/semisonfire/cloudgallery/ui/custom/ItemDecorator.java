package com.semisonfire.cloudgallery.ui.custom;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.semisonfire.cloudgallery.ui.main.disk.adapter.items.DiskItem;

public class ItemDecorator extends RecyclerView.ItemDecoration {

    private int space;

    public ItemDecorator(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int position = parent.getChildAdapterPosition(view);

        if (layoutManager instanceof GridLayoutManager) {
            setGridSpace(outRect, (GridLayoutManager) layoutManager, position);
        } else if (layoutManager instanceof LinearLayoutManager) {
            setLinearSpace(outRect, (LinearLayoutManager) layoutManager, parent.getAdapter(), position);
        }
    }

    private void setGridSpace(Rect outRect, GridLayoutManager gridLayoutManager, int position) {
        int spans = gridLayoutManager.getSpanCount();
        int rows = position / spans;
        outRect.left = space;
        outRect.right = position % spans == spans - 1 ? space : 0;
        outRect.top = space;
        outRect.bottom = position / spans == rows - 1 ? space : 0;
    }

    private void setLinearSpace(Rect outRect, LinearLayoutManager linearLayoutManager, RecyclerView.Adapter adapter, int position) {
        if (linearLayoutManager.canScrollHorizontally()) {
            outRect.left = space;
            outRect.right = position == (adapter.getItemCount() - 1) ? space : 0;
        } else {
            outRect.top = position == 0 ? space : 0;
            outRect.bottom = space;
            if (position == -1) {
                outRect.bottom = 0;
            } else if (adapter.getItemViewType(position) == DiskItem.TYPE_HEADER) {
                outRect.bottom = 0;
            }
        }
    }
}
