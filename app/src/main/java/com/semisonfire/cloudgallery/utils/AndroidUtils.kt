package com.semisonfire.cloudgallery.utils

import android.content.Context
import android.support.annotation.DimenRes
import android.support.annotation.StringRes
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun background() = Schedulers.computation()
fun foreground() = AndroidSchedulers.mainThread()

fun Context.dipF(value: Int): Float = (value * resources.displayMetrics.density)
fun Context.dipF(value: Float): Float = (value * resources.displayMetrics.density)
fun Context.dip(value: Int): Int = dipF(value).toInt()
fun Context.dip(value: Float): Int = dipF(value).toInt()
fun Context.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.sp(value: Float): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

fun Context.shortToast(text: String) =
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

fun Context.shortToast(@StringRes stringId: Int) =
    Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show()

fun Context.longToast(text: String) =
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()

fun Context.longToast(@StringRes stringId: Int) =
    Toast.makeText(this, stringId, Toast.LENGTH_LONG).show()