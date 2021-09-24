package com.semisonfire.cloudgallery.ui.disk

import androidx.fragment.app.Fragment
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.ui.navigation.Navigator

const val DISK_KEY = "DISK_KEY"
const val DISK_CONTAINER_ID = R.id.frame_fragment

class DiskNavigator : Navigator() {
    override val key: String
        get() = DISK_KEY
    override val containerId: Int
        get() = DISK_CONTAINER_ID

    override fun createFragment(key: String, bundle: Any?): Fragment? {
        return when (key) {
            DISK_KEY -> DiskFragment()
            else -> null
        }
    }
}