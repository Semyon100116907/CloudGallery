package com.semisonfire.cloudgallery.navigation.utils

import android.os.Bundle
import android.os.Parcelable

fun <V> Map<String, V>.toBundle(bundle: Bundle = Bundle()): Bundle = bundle.apply {
    forEach {
        val key = it.key
        when (val value = it.value) {
            is Bundle -> putBundle(key, value)
            is Char -> putChar(key, value)
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Float -> putFloat(key, value)
            is Long -> putLong(key, value)
            is Double -> putDouble(key, value)
            is CharSequence -> putCharSequence(key, value)
            is Parcelable -> putParcelable(key, value)
            else -> throw IllegalArgumentException("$value is of a type that is not currently supported")
        }
    }
}