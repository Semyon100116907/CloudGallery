package com.semisonfire.cloudgallery.ui.disk.adapter.upload

import android.view.View
import com.semisonfire.cloudgallery.R.dimen
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import com.semisonfire.cloudgallery.databinding.ItemUploadPhotoBinding
import com.semisonfire.cloudgallery.image.utils.loadRoundedImage
import com.semisonfire.cloudgallery.utils.dimen

class UploadItemViewHolder(view: View) : ItemViewHolder<UploadItem>(view) {

    private val viewBinding = ItemUploadPhotoBinding.bind(view)

    private val targetHeight = context.dimen(dimen.photo_min_height)
    private val targetWidth = context.dimen(dimen.photo_min_width)

    override fun bind(item: UploadItem) {
        super.bind(item)
        viewBinding.imagePhoto.loadRoundedImage(item.imagePath, targetWidth, targetHeight)
    }
}