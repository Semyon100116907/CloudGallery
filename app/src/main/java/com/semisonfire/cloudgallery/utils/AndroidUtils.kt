package com.semisonfire.cloudgallery.utils

import android.content.Context
import android.support.annotation.DimenRes
import android.support.v4.app.Fragment
import android.view.View
import com.semisonfire.cloudgallery.App

fun background() = App.background.value
fun foreground() = App.foreground.value

fun Context.dipF(value: Int): Float = (value * resources.displayMetrics.density)
fun Context.dipF(value: Float): Float = (value * resources.displayMetrics.density)
fun Context.dip(value: Int): Int = dipF(value).toInt()
fun Context.dip(value: Float): Int = dipF(value).toInt()
fun Context.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.sp(value: Float): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

fun View.dip(value: Int): Int = context.dip(value)
fun View.dip(value: Float): Int = context.dip(value)
fun View.sp(value: Int): Int = context.sp(value)
fun View.sp(value: Float): Int = context.sp(value)
fun View.dimen(@DimenRes resource: Int): Int = context.dimen(resource)

fun Fragment.dip(value: Int): Int = activity!!.dip(value)
fun Fragment.dip(value: Float): Int = activity!!.dip(value)
fun Fragment.sp(value: Int): Int = activity!!.sp(value)
fun Fragment.sp(value: Float): Int = activity!!.sp(value)
fun Fragment.dimen(@DimenRes resource: Int): Int = activity!!.dimen(resource)

fun Long.toSeconds() = (this / 1000).toInt()