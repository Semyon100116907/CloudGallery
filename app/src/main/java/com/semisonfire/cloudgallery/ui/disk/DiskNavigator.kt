package com.semisonfire.cloudgallery.ui.disk

import androidx.fragment.app.Fragment
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.navigation.ScreenKey
import com.semisonfire.cloudgallery.navigation.navigator.Navigator
import com.semisonfire.cloudgallery.navigation.navigator.NavigatorContainer
import javax.inject.Inject

class DiskNavigator @Inject constructor() : Navigator {

    override val key: String = ScreenKey.DISK.name

    override fun container(): NavigatorContainer {
        return NavigatorContainer(
            containerId = R.id.frame_fragment
        )
    }

    override fun createFragment(): Fragment {
        return DiskFragment()
    }
}