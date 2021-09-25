package com.semisonfire.cloudgallery.navigation.destination

import com.semisonfire.cloudgallery.navigation.ScreenKey
import com.semisonfire.cloudgallery.navigation.navigator.NavigatorConfiguration

data class Destination(
    val screenKey: ScreenKey,
    val params: Map<String, Any>? = null,
    val configuration: NavigatorConfiguration? = null
)