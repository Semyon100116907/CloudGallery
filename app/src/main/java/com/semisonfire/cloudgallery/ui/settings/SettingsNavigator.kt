package com.semisonfire.cloudgallery.ui.settings

import androidx.fragment.app.Fragment
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.navigation.ScreenKey
import com.semisonfire.cloudgallery.navigation.navigator.Navigator
import com.semisonfire.cloudgallery.navigation.navigator.NavigatorContainer
import javax.inject.Inject

const val SETTINGS_KEY = "SETTINGS_KEY"

class SettingsNavigator @Inject constructor() : Navigator {

    override val key: String = ScreenKey.SETTINGS.name

    override fun container(): NavigatorContainer {
        return NavigatorContainer(
            containerId = R.id.frame_fragment
        )
    }

    override fun createFragment(): Fragment {
        return SettingsFragment()
    }
}