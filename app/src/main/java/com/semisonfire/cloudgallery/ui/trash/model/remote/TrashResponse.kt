package com.semisonfire.cloudgallery.ui.trash.model.remote

import com.google.gson.annotations.SerializedName
import com.semisonfire.cloudgallery.data.remote.api.ItemsResponse

data class TrashBinResponse(
    @SerializedName("_embedded")
    val trashResponse: ItemsResponse? = null
)