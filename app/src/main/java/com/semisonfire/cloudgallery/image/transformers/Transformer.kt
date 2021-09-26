package com.semisonfire.cloudgallery.image.transformers

import android.graphics.Bitmap

interface Transformer {
    val transformerName: String
    fun transform(source: Bitmap?): Bitmap
}