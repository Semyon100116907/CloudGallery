package com.semisonfire.cloudgallery.ui.disk.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.ui.adapter.BaseAdapter
import com.semisonfire.cloudgallery.core.ui.adapter.BaseViewHolder
import com.semisonfire.cloudgallery.image.utils.loadRoundedImage
import com.semisonfire.cloudgallery.ui.custom.PhotoDiffUtil
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper.OnPhotoListener
import com.semisonfire.cloudgallery.ui.disk.adapter.PhotoAdapter.PhotoViewHolder
import com.semisonfire.cloudgallery.utils.dimen

class PhotoAdapter : BaseAdapter<Photo, PhotoViewHolder>() {

    private var photoListener: OnPhotoListener? = null

    fun setPhotoListener(photoListener: OnPhotoListener) {
        this.photoListener = photoListener
    }

    override fun layoutId(): Int {
        return R.layout.item_photo
    }

    override fun createViewHolder(view: View): PhotoViewHolder {
        val viewHolder = PhotoViewHolder(view)
        addClickListener(viewHolder)
        return viewHolder
    }

    private fun addClickListener(photoViewHolder: PhotoViewHolder) {
        val itemClick = View.OnClickListener {
            val adapterPosition = photoViewHolder.adapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@OnClickListener

            val photo = items[adapterPosition]
            if (!SelectableHelper.getMultipleSelection()) {
                photoListener?.onPhotoClick(items, adapterPosition)
            } else {
                photo.isSelected = !photo.isSelected
                notifyItemChanged(adapterPosition)
                photoListener?.onSelectedPhotoClick(photo)
            }
        }
        photoViewHolder.photoImage.setOnClickListener(itemClick)
        photoViewHolder.photoImage.setOnLongClickListener {
            val adapterPosition = photoViewHolder.adapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnLongClickListener false

            if (photoListener != null && !SelectableHelper.getMultipleSelection()) {
                val photo = items[adapterPosition]
                photo.isSelected = true
                notifyItemChanged(adapterPosition)

                photoListener?.onPhotoLongClick()
                photoListener?.onSelectedPhotoClick(photo)
                return@setOnLongClickListener true
            }
            false
        }
    }

    override fun updateDataSet(newItems: List<Photo>) {
        val diffUtilCallback = PhotoDiffUtil(newItems, items)
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setSelection(selected: Boolean) {
        if (!selected) {
            for (photo in items) {
                if (photo.isSelected) {
                    photo.isSelected = false
                }
            }
        }
        SelectableHelper.setMultipleSelection(selected)
        notifyDataSetChanged()
    }

    class PhotoViewHolder(itemView: View) : BaseViewHolder<Photo>(itemView) {

        val photoImage: ImageView = itemView.findViewById(R.id.image_photo)
        private val selectImage: ImageView = itemView.findViewById(R.id.image_selected)

        private val targetHeight = itemView.context.dimen(R.dimen.photo_max_height)
        private val targetWidth = itemView.context.dimen(R.dimen.photo_max_width)

        override fun bindItem(item: Photo) {
            photoImage.setImageDrawable(null)
            photoImage.loadRoundedImage(item.preview, targetWidth, targetHeight)
            selectImage.visibility = if (item.isSelected) View.VISIBLE else View.GONE
        }
    }
}