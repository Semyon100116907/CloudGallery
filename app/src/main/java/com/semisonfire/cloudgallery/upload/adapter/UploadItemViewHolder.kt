package com.semisonfire.cloudgallery.upload.adapter

import android.view.View
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import com.semisonfire.cloudgallery.databinding.ItemUploadPhotoBinding
import com.semisonfire.cloudgallery.image.utils.loadRoundedImage
import com.semisonfire.cloudgallery.utils.dip

class UploadItemViewHolder(view: View) : ItemViewHolder<UploadItem>(view) {

    private val viewBinding = ItemUploadPhotoBinding.bind(view)

    private val targetHeight = context.dip(150)
    private val targetWidth = context.dip(150)

    override fun bind(item: UploadItem) {
        super.bind(item)
        viewBinding.imagePhoto.loadRoundedImage(item.imagePath, targetWidth, targetHeight)
    }
}