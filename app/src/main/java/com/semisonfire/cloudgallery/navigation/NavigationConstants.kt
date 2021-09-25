package com.semisonfire.cloudgallery.navigation

import android.net.Uri

object NavigationConstants {
    const val SCHEME = "cloud"

    const val SCREEN_KEY = "screenKey"

    const val ID = "id"
    const val TYPE = "type"
}

object LinkGenerator {

    private val defaultBuilder = Uri.Builder()
        .scheme(NavigationConstants.SCHEME)
        .build()

}