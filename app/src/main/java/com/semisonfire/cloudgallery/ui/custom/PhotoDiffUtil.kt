package com.semisonfire.cloudgallery.ui.custom

import android.support.v7.util.DiffUtil
import com.semisonfire.cloudgallery.core.data.model.Photo

class PhotoDiffUtil(
    private val newList: List<Photo>,
    private val oldList: List<Photo>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newItem = newList[newItemPosition]
        val oldItem = oldList[oldItemPosition]
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val newItem = newList[newItemPosition]
        val oldItem = oldList[oldItemPosition]
        return oldItem == newItem
    }

}