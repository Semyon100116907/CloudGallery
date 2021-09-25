package com.semisonfire.cloudgallery.navigation.router

import androidx.fragment.app.FragmentManager
import com.semisonfire.cloudgallery.navigation.destination.Destination
import com.semisonfire.cloudgallery.navigation.internal.NavigateCoordinator
import javax.inject.Inject

internal class RouterImpl @Inject constructor(
    private val navigator: NavigateCoordinator
) : Router {

    override fun bind(fragmentManager: FragmentManager) {
        navigator.bind(fragmentManager)
    }

    override fun open(destination: Destination) {
        navigator.open(destination)
    }

    override fun openBottomSheet(destination: Destination) {
        navigator.openBottomSheet(destination)
    }

    override fun replaceScreen(destination: Destination) {
        navigator.replace(destination)
    }

    override fun back() {
        navigator.back()
    }

    override fun clear() {
        navigator.clear()
    }

    override fun unbind() {
        navigator.unbind()
    }
}