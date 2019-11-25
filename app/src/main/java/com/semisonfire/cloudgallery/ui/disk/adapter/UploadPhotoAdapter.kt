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
import com.semisonfire.cloudgallery.utils.dimen
import com.squareup.picasso.Picasso
import java.io.File

class UploadPhotoAdapter :
  RecyclerView.Adapter<UploadPhotoAdapter.UploadViewHolder>() {

  private val photoList = mutableListOf<Photo>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(
      R.layout.item_photo,
      parent,
      false
    )
    return UploadViewHolder(view)
  }

  override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
    holder.bind(photoList[position])
  }

  fun setItems(items: List<Photo>) {
    val diffUtilCallback = PhotoDiffUtil(items, photoList)
    val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
    photoList.clear()
    photoList.addAll(items)
    diffResult.dispatchUpdatesTo(this)
  }

  override fun getItemCount(): Int {
    return photoList.size
  }

  inner class UploadViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    private val targetHeight = itemView.context.dimen(R.dimen.photo_min_height)
    private val targetWidth = itemView.context.dimen(R.dimen.photo_min_width)

    private val photoImageView: ImageView = itemView.findViewById(R.id.image_photo)

    fun bind(photo: Photo) {
      Picasso.get()
        .load(File(photo.localPath))
        .resize(targetWidth, targetHeight)
        .onlyScaleDown()
        .centerCrop()
        .into(photoImageView)
    }
  }
}