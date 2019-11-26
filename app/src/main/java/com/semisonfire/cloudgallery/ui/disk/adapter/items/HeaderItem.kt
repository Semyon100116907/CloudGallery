package com.semisonfire.cloudgallery.ui.disk.adapter.items

class HeaderItem : DiskItem() {

  var date: String? = null
  var count = 0

  override val type: Int
    get() = TYPE_HEADER

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is HeaderItem) return false

    if (date != other.date) return false

    return true
  }

  override fun hashCode(): Int {
    return date?.hashCode() ?: 0
  }
}