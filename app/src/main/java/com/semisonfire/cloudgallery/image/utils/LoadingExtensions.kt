package com.semisonfire.cloudgallery.image.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

fun ImageView.loadCircleImage(
    uri: String,
    width: Int? = null,
    height: Int? = null,
    @DrawableRes placeholderId: Int? = null
) {
    imageRequest(uri, placeholderId, width, height)
        .transform(CircleCrop())
        .into(this)
}

fun ImageView.loadRoundedImage(
    uri: String,
    width: Int? = null,
    height: Int? = null,
    @DrawableRes placeholderId: Int? = null
) {
    imageRequest(uri, placeholderId, width, height)
        .transform(CenterCrop(), RoundedCorners(context.dip(4)))
        .into(this)
}

private fun ImageView.imageRequest(
    uri: String,
    placeholderId: Int?,
    width: Int?,
    height: Int?
): RequestBuilder<Drawable> {
    return Glide.with(this)
        .load(uri)
        .let {
            if (placeholderId == null) {
                it
            } else {
                it.placeholder(placeholderId)
            }
        }
        .let {
            if (width != null && height != null) {
                it.apply(RequestOptions.overrideOf(width, height))
            } else {
                it
            }
        }
        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
}

private fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()