package com.semisonfire.cloudgallery.ui.disk.data

import com.semisonfire.cloudgallery.utils.DateUtils

data class DiskKey(
    val date: String
) {
    val millis = DateUtils.dateMillis(date)
}