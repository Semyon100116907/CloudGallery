package com.semisonfire.cloudgallery.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.Menu
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
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

fun Context.string(@StringRes resId: Int): String = getString(resId)

fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)
fun Context.colorDrawable(@ColorInt color: Int) = ColorDrawable(color)
fun Context.colorResDrawable(@ColorRes colorRes: Int): ColorDrawable {
    val color = color(colorRes)
    return ColorDrawable(color)
}

/** Change menu items icon color  */
fun setMenuIconsColor(menu: Menu, @ColorInt color: Int) {
    for (i in 0 until menu.size()) {
        val drawable = menu.getItem(i).icon
        if (drawable != null) {
            drawable.mutate()
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}

fun Context.shortToast(text: String) =
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

fun Context.shortToast(@StringRes stringId: Int) =
    Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show()

fun Context.longToast(text: String) =
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()

fun Context.longToast(@StringRes stringId: Int) =
    Toast.makeText(this, stringId, Toast.LENGTH_LONG).show()