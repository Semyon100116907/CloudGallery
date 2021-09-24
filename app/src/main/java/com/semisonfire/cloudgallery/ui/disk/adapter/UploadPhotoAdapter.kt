package com.semisonfire.cloudgallery.ui.disk.adapter

import android.support.v7.util.DiffUtil
import android.view.View
import android.widget.ImageView
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.ui.adapter.BaseAdapter
import com.semisonfire.cloudgallery.core.ui.adapter.BaseViewHolder
import com.semisonfire.cloudgallery.ui.custom.PhotoDiffUtil
import com.semisonfire.cloudgallery.utils.dimen
import com.squareup.picasso.Picasso
import java.io.File

class UploadPhotoAdapter :
    BaseAdapter<Photo, UploadPhotoAdapter.UploadViewHolder>() {

    override fun layoutId(): Int {
        return R.layout.item_photo
    }

    override fun createViewHolder(view: View): UploadViewHolder {
        return UploadViewHolder(view)
    }

    override fun updateDataSet(newItems: List<Photo>) {
        val diffUtilCallback = PhotoDiffUtil(newItems, items)
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class UploadViewHolder(itemView: View) : BaseViewHolder<Photo>(itemView) {

        private val targetHeight = itemView.context.dimen(R.dimen.photo_min_height)
        private val targetWidth = itemView.context.dimen(R.dimen.photo_min_width)

        private val photoImageView: ImageView = itemView.findViewById(R.id.image_photo)

        override fun bindItem(item: Photo) {
            Picasso.get()
                .load(File(item.localPath))
                .resize(targetWidth, targetHeight)
                .onlyScaleDown()
                .centerCrop()
                .into(photoImageView)
        }
    }
}