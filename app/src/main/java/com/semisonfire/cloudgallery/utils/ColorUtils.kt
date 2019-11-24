package com.semisonfire.cloudgallery.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.Menu

fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)
fun Context.colorDrawable(@ColorInt color: Int) = ColorDrawable(color)
fun Context.colorResDrawable(@ColorRes colorRes: Int): ColorDrawable? {
    val color = color(colorRes)
    return ColorDrawable(color)
}

/** Change menu items icon color  */
fun setMenuIconsColor(menu: Menu, color: Int) {
    for (i in 0 until menu.size()) {
        val drawable = menu.getItem(i).icon
        if (drawable != null) {
            drawable.mutate()
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}