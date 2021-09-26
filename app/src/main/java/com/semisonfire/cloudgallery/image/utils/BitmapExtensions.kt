package com.semisonfire.cloudgallery.image.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.semisonfire.cloudgallery.image.transformers.Transformer
import java.io.FileNotFoundException

fun Context.loadBitmapFromDescriptor(uri: String): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            retrieveBitmapFileDescriptor(uri)
        } catch (e: FileNotFoundException) {
            null
        }
    } else {
        BitmapFactory.decodeFile(uri, BitmapFactory.Options())
    }
}

/**
 * Android Q file descriptor support
 * @see [java.io.FileDescriptor]
 */
@RequiresApi(Build.VERSION_CODES.Q)
private fun Context.retrieveBitmapFileDescriptor(uri: String): Bitmap? {

    contentResolver.openFileDescriptor(Uri.parse(uri), "r")?.use {
        return BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }

    return null
}

fun Context.loadBitmapFromResources(
    @DrawableRes drawableRes: Int,
    @Px size: Int? = null,
    transformers: List<Transformer> = emptyList()
): Bitmap {
    return loadBitmapFromResources(drawableRes, size, size, transformers)
}

fun Context.loadBitmapFromResources(
    resId: Int,
    @Px width: Int? = null,
    @Px height: Int? = null,
    transformers: List<Transformer> = emptyList()
): Bitmap {
    var resourcesBitmap = BitmapFactory.decodeResource(resources, resId)
    if (resourcesBitmap == null) {
        val drawable = ContextCompat.getDrawable(this, resId)
        if (drawable != null) {
            resourcesBitmap = if (width != null && height != null) {
                drawable.toBitmap(width, height)
            } else {
                drawable.toBitmap()
            }
        }
    }

    return transformBitmap(resourcesBitmap, transformers)
}

private fun transformBitmap(
    bitmap: Bitmap,
    transformers: List<Transformer>
): Bitmap {
    var transformedBitmap: Bitmap = bitmap
    transformers.forEach {
        transformedBitmap = it.transform(transformedBitmap)
    }

    return transformedBitmap
}