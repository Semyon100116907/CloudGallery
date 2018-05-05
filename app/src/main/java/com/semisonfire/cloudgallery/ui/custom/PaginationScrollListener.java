package com.semisonfire.cloudgallery.ui.custom;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {

    private int limit;

    public PaginationScrollListener(int limit) {
        this.limit = limit;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        int position = layoutManager.findLastVisibleItemPosition();
        int updatePosition = recyclerView.getAdapter().getItemCount() - 1 - (limit / 2);

        if (!isLoading() && !isLastPage() && position >= updatePosition) {
            loadNext();
        }

    }

    public abstract void loadNext();

    public abstract boolean isLoading();

    public abstract boolean isLastPage();
}
