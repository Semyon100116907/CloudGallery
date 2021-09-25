package com.semisonfire.cloudgallery.navigation.router

import androidx.fragment.app.FragmentManager
import com.semisonfire.cloudgallery.navigation.destination.Destination

interface Router {
    fun bind(fragmentManager: FragmentManager)
    fun unbind()

    fun open(destination: Destination)
    fun openBottomSheet(destination: Destination)

    fun replaceScreen(destination: Destination)

    fun back()
    fun clear()
}