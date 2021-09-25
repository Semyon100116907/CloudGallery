package com.semisonfire.cloudgallery.ui.photo

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.viewpager.widget.PagerAdapter
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import java.util.ArrayList

class PhotoDetailAdapter : PagerAdapter() {

    private var photoList: List<Photo> = ArrayList()
    private var orientation = 0

    var currentItemBitmap: Bitmap? = null
        private set

    fun getItemByPosition(position: Int): Photo? {
        return if (photoList.isNotEmpty()) photoList[position] else null
    }

    fun setItems(mPhotos: List<Photo>) {
        photoList = mPhotos
        notifyDataSetChanged()
    }

    fun setOrientation(orientation: Int) {
        this.orientation = orientation
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any { //Bind view
        val view = LayoutInflater.from(container.context).inflate(
            R.layout.item_photo_detail,
            container,
            false
        )

        val photoImageView = view.findViewById<ImageView>(R.id.image_photo_detailed)
        val progressLoader = view.findViewById<ProgressBar>(R.id.progress_photo_loading)
        photoImageView.layoutParams = photoImageView.layoutParams?.apply {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        var maxWidth = 0
        var maxHeight = 0

        val windowManager = view.context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        windowManager?.let {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            maxWidth = size.x
            maxHeight = size.y
        }

        photoImageView.maxWidth = maxWidth
        photoImageView.maxHeight = maxHeight

        progressLoader.visibility = View.VISIBLE

        //Load image
        val loadTarget = LoadTarget(photoImageView, progressLoader)
        val photo = photoList[position]
        Picasso.get()
            .load(photo.preview)
            .transform(FullScreenTransform(maxWidth))
            .noFade()
            .placeholder(R.color.color_black)
            .into(loadTarget)
        photoImageView.tag = loadTarget
        container.addView(view)
        return view
    }

    override fun getCount(): Int {
        return photoList.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    internal inner class LoadTarget(
        private val imageView: ImageView,
        private val progressBar: ProgressBar
    ) : Target {

        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
            currentItemBitmap = bitmap
            imageView.setImageBitmap(bitmap)
            progressBar.visibility = View.GONE
        }

        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
            progressBar.visibility = View.GONE
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
    }

    internal inner class FullScreenTransform(private val targetWidth: Int) : Transformation {

        override fun transform(source: Bitmap): Bitmap {
            val aspectRatio = source.height.toDouble() / source.width.toDouble()
            val height = (targetWidth * aspectRatio).toInt()
            val transformed = Bitmap.createScaledBitmap(source, targetWidth, height, false)
            if (transformed != source) {
                source.recycle()
                return transformed
            }
            return source
        }

        override fun key(): String {
            return "transform_$targetWidth"
        }

    }
}