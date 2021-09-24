package com.semisonfire.cloudgallery.ui.disk.adapter.items

const val TYPE_HEADER = 0
const val TYPE_GALLERY = 1
const val TYPE_UPLOAD = 2
const val TYPE_PROGRESS = 3

abstract class DiskItem {
    /** Item type.  */
    abstract val type: Int
}

class ProgressItem : DiskItem() {
    override val type: Int
        get() = TYPE_PROGRESS
}