package com.semisonfire.cloudgallery.ui.trash

import androidx.fragment.app.Fragment
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.navigation.ScreenKey
import com.semisonfire.cloudgallery.navigation.navigator.Navigator
import com.semisonfire.cloudgallery.navigation.navigator.NavigatorContainer
import javax.inject.Inject

class TrashBinNavigator @Inject constructor() : Navigator {

    override val key: String = ScreenKey.TRASH_BIN.name

    override fun container(): NavigatorContainer {
        return NavigatorContainer(
            containerId = R.id.fragment_container
        )
    }

    override fun createFragment(): Fragment {
        return TrashFragment()
    }
}