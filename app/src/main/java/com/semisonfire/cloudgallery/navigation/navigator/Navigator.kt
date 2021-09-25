package com.semisonfire.cloudgallery.navigation.navigator

import androidx.fragment.app.Fragment

interface Navigator {

    val key: String

    fun container(): NavigatorContainer

    fun createFragment(): Fragment
}