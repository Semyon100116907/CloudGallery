package com.semisonfire.cloudgallery.ui.trash.model

import com.semisonfire.cloudgallery.adapter.holder.Item

sealed class TrashBinResult {

    data class Loaded(val photos: List<Item>) : TrashBinResult()
    object Cleared : TrashBinResult()
}