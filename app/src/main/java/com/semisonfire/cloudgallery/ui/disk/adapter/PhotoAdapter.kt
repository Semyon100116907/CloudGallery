package com.semisonfire.cloudgallery.ui.disk.adapter

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.ui.custom.PhotoDiffUtil
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper.OnPhotoListener
import com.semisonfire.cloudgallery.ui.disk.adapter.PhotoAdapter.PhotoViewHolder
import com.semisonfire.cloudgallery.utils.dimen
import com.squareup.picasso.Picasso

class PhotoAdapter : RecyclerView.Adapter<PhotoViewHolder>() {

  private val photos = mutableListOf<Photo>()
  private var photoListener: OnPhotoListener? = null

  fun setPhotoListener(photoListener: OnPhotoListener) {
    this.photoListener = photoListener
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(
      R.layout.item_photo,
      parent,
      false
    )
    val photoViewHolder = PhotoViewHolder(view)
    addClickListener(photoViewHolder)
    return photoViewHolder
  }

  private fun addClickListener(photoViewHolder: PhotoViewHolder) {
    val onItemClick = View.OnClickListener {
      val adapterPosition = photoViewHolder.adapterPosition
      if (adapterPosition == RecyclerView.NO_POSITION) return@OnClickListener

      val photo = photos[adapterPosition]
      if (!SelectableHelper.getMultipleSelection()) {
        photoListener?.onPhotoClick(photos, adapterPosition)
      } else {
        photo.isSelected = !photo.isSelected
        notifyItemChanged(adapterPosition)
        photoListener?.onSelectedPhotoClick(photo)
      }
    }
    photoViewHolder.selectImage.setOnClickListener(onItemClick)
    photoViewHolder.photoImage.setOnClickListener(onItemClick)

    photoViewHolder.photoImage.setOnLongClickListener {
      val adapterPosition = photoViewHolder.adapterPosition
      if (adapterPosition == RecyclerView.NO_POSITION) return@setOnLongClickListener false

      if (photoListener != null && !SelectableHelper.getMultipleSelection()) {
        val photo = photos[adapterPosition]
        photo.isSelected = true
        notifyItemChanged(adapterPosition)

        photoListener?.onPhotoLongClick()
        photoListener?.onSelectedPhotoClick(photo)
        return@setOnLongClickListener true
      }
      false
    }
  }

  override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
    holder.bind(photos[position])
  }

  override fun getItemCount(): Int {
    return photos.size
  }

  fun setPhotos(items: List<Photo>) {
    val diffUtilCallback = PhotoDiffUtil(items, photos)
    val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
    photos.clear()
    photos.addAll(items)
    diffResult.dispatchUpdatesTo(this)
  }

  fun addPhotos(photos: List<Photo>) {
    this.photos.addAll(photos)
    notifyItemRangeInserted(itemCount, photos.size)
  }

  fun setSelection(selected: Boolean) {
    if (!selected) {
      for (photo in photos) {
        if (photo.isSelected) {
          photo.isSelected = false
        }
      }
    }
    SelectableHelper.setMultipleSelection(selected)
    notifyDataSetChanged()
  }

  fun remove(photo: Photo) {
    val position = photos.indexOf(photo)
    photos.remove(photo)

    notifyItemRemoved(position)
  }

  inner class PhotoViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    val photoImage: ImageView = itemView.findViewById(R.id.image_photo)
    val selectImage: ImageView = itemView.findViewById(R.id.image_selected)

    private val targetHeight = itemView.context.dimen(R.dimen.photo_max_height)
    private val targetWidth = itemView.context.dimen(R.dimen.photo_max_width)

    fun bind(photo: Photo) {

      photoImage.setImageDrawable(null)
      Picasso.get().load(photo.preview)
        .resize(targetWidth, targetHeight)
        .centerCrop()
        .placeholder(R.color.black)
        .error(R.drawable.ic_gallery)
        .into(photoImage)
      selectImage.visibility = if (photo.isSelected) View.VISIBLE else View.GONE
    }
  }
}