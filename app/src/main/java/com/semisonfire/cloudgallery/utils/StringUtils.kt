package com.semisonfire.cloudgallery.utils

import android.content.Context
import android.support.annotation.StringRes

fun Context.string(@StringRes resId: Int): String = getString(resId)