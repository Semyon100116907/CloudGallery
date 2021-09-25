package com.semisonfire.cloudgallery.adapter.holder

interface Item {
    fun areItemsTheSame(item: Item): Boolean
    fun areContentTheSame(item: Item): Boolean
}