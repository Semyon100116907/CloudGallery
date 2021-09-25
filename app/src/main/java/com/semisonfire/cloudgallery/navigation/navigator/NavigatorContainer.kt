package com.semisonfire.cloudgallery.navigation.navigator

data class NavigatorContainer(
    val containerId: Int,
    val configuration: NavigatorConfiguration? = null
) {

    companion object {
        const val CONTAINER_NONE = -1
    }
}