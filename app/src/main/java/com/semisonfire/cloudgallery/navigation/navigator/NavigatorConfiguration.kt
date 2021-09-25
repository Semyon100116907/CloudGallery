package com.semisonfire.cloudgallery.navigation.navigator

import android.os.Parcelable

interface NavigatorConfiguration : Parcelable {

    companion object {
        const val NAVIGATOR_CONFIGURATION_ARG = "NAVIGATOR_CONFIGURATION_ARG"
    }
}