package com.semisonfire.cloudgallery.ui.trash.model.remote

import com.google.gson.annotations.SerializedName
import com.semisonfire.cloudgallery.core.data.model.Photo

class Trash {
    @SerializedName("_embedded")
    var trashResponse: TrashResponse? = null
}

class TrashResponse {
    @SerializedName("items")
    var photos: List<Photo>? = null
}