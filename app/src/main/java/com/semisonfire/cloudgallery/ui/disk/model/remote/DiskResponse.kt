package com.semisonfire.cloudgallery.ui.disk.model.remote

import com.semisonfire.cloudgallery.core.data.model.Photo

data class DiskResponse(
    val items: List<Photo>? = null
)