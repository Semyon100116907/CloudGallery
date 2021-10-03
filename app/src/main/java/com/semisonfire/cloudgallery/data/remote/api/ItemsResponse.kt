package com.semisonfire.cloudgallery.data.remote.api

import com.semisonfire.cloudgallery.data.model.Photo

data class ItemsResponse(
    val items: List<Photo>? = null
)