package com.semisonfire.cloudgallery.common.photo

import android.view.View
import androidx.core.view.isVisible
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder
import com.semisonfire.cloudgallery.databinding.ItemPhotoBinding
import com.semisonfire.cloudgallery.utils.dimen
import com.squareup.picasso.Picasso

class PhotoViewHolder(view: View) : ItemViewHolder<PhotoItem>(view) {

    private val viewBinding = ItemPhotoBinding.bind(view)

    private val targetHeight = itemView.context.dimen(R.dimen.photo_max_height)
    private val targetWidth = itemView.context.dimen(R.dimen.photo_max_width)

    init {
        viewBinding.imageSelected.isVisible = false
    }

    override fun bind(item: PhotoItem) {
        super.bind(item)
        Picasso.get().load(item.url)
            .placeholder(R.color.color_black)
            .error(R.drawable.ic_gallery)
            .resize(targetWidth, targetHeight)
            .centerCrop()
            .into(viewBinding.imagePhoto)
    }
}