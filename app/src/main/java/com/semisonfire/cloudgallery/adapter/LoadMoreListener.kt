package com.semisonfire.cloudgallery.adapter

interface LoadMoreListener {
    fun loadMore(position: Int) {}
    fun loadMoreComplete() {}
}