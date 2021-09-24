package com.semisonfire.cloudgallery.ui.trash

import android.support.v4.app.Fragment
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.ui.navigation.Navigator

const val TRASH_KEY = "TRASH_KEY"
const val TRASH_CONTAINER_ID = R.id.frame_fragment

class TrashNavigator : Navigator() {
    override val key: String
        get() = TRASH_KEY
    override val containerId: Int
        get() = TRASH_CONTAINER_ID

    override fun createFragment(key: String, bundle: Any?): Fragment? {
        return when (key) {
            TRASH_KEY -> TrashFragment()
            else -> null
        }
    }
}