package com.semisonfire.cloudgallery.ui.disk.adapter.items

import com.semisonfire.cloudgallery.data.model.Photo

class UploadItem : DiskItem() {
    private val uploadPhotos: MutableList<Photo>

    var size = 0
    var uploadCount: Int
        private set
    var visibility = 0
    var state: String? = null

    fun getUploadPhotos(): MutableList<Photo> {
        return uploadPhotos
    }

    fun addUploadPhotos(photos: List<Photo>) {
        uploadPhotos.addAll(photos)
        size = uploadPhotos.size
    }

    fun resetUploadCount() {
        uploadCount = 1
    }

    fun incrementUpload() {
        ++uploadCount
    }

    override val type: Int
        get() = TYPE_UPLOAD

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UploadItem) return false

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return type
    }

    init {
        uploadPhotos = ArrayList()
        uploadCount = 1
    }
}