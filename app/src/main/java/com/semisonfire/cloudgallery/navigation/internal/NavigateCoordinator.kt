package com.semisonfire.cloudgallery.navigation.internal

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.semisonfire.cloudgallery.navigation.destination.Destination
import com.semisonfire.cloudgallery.navigation.navigator.Navigator
import com.semisonfire.cloudgallery.navigation.navigator.NavigatorConfiguration
import com.semisonfire.cloudgallery.navigation.utils.toBundle
import javax.inject.Inject

internal class NavigateCoordinator @Inject constructor(
    private val navigators: Map<String, @JvmSuppressWildcards Navigator>
) {

    private var fragmentManager: FragmentManager? = null

    fun bind(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun unbind() {
        this.fragmentManager = null
    }

    fun open(destination: Destination) {
        val fragmentManager = this.fragmentManager ?: return

        val key = destination.screenKey.name

        val navigator = getNavigatorByKey(key)
        val navigatorContainer = navigator.container()
        val navigatorContainerId = navigatorContainer.containerId

        val bundle = destination.params?.toBundle() ?: Bundle()
        val fragment = navigator.createFragment()
            .appendConfiguration(
                destination.configuration ?: navigatorContainer.configuration,
                bundle
            )

        fragmentManager
            .beginTransaction()
            .add(navigatorContainerId, fragment, key)
            .addToBackStack(key)
            .commit()
    }

    fun replace(destination: Destination) {
        val fragmentManager = fragmentManager ?: return

        val key = destination.screenKey.name

        val fragmentByTag = fragmentManager.findFragmentByTag(key)
        if (fragmentByTag == null) {

            val navigator = getNavigatorByKey(key)
            val navigatorContainer = navigator.container()
            val navigatorContainerId = navigatorContainer.containerId

            val bundle = destination.params?.toBundle() ?: Bundle()
            val fragment = navigator.createFragment()
                .appendConfiguration(
                    destination.configuration ?: navigatorContainer.configuration,
                    bundle
                )

            fragmentManager.beginTransaction().apply {
                replace(navigatorContainerId, fragment, key)

                commit()
            }
        } else {
            fragmentManager
                .beginTransaction()
                .show(fragmentByTag)
                .commit()
        }
    }

    fun openBottomSheet(destination: Destination) {
        val fragmentManager = fragmentManager ?: return

        val key = destination.screenKey.name

        val navigator = getNavigatorByKey(key)
        val navigatorContainer = navigator.container()

        val bundle = destination.params?.toBundle() ?: Bundle()
        val fragment = navigator.createFragment()
            .appendConfiguration(
                destination.configuration ?: navigatorContainer.configuration,
                bundle
            )
        if (fragment is BottomSheetDialogFragment) {
            fragment.show(fragmentManager, key)
        }
    }

    fun back() {
        val fragmentManager = fragmentManager ?: return
        val lastFragment = fragmentManager.fragments.lastOrNull()
        if (lastFragment is BottomSheetDialogFragment) {
            lastFragment.dismiss()
        } else {
            fragmentManager.popBackStack()
        }
    }

    fun clear() {
        val manager = fragmentManager ?: return
        val fragments = manager.fragments
        val fragmentsCount = fragments.size
        if (fragmentsCount == 1) return

        val transaction = manager.beginTransaction()
        for (index in 1 until fragmentsCount) {
            val fragment = fragments[index]
            transaction.remove(fragment)
        }
        transaction.commit()
    }

    private fun Fragment.appendConfiguration(
        configuration: NavigatorConfiguration?,
        bundle: Bundle
    ): Fragment {
        configuration?.let {
            bundle.putParcelable(
                NavigatorConfiguration.NAVIGATOR_CONFIGURATION_ARG, it
            )
            arguments = bundle
        }

        return this
    }

    private fun getNavigatorByKey(key: String): Navigator =
        navigators[key] ?: throw IllegalStateException("Navigator could not be null")
}