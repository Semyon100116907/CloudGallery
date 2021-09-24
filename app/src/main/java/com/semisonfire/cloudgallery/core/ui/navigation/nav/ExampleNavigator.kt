package com.semisonfire.cloudgallery.core.ui.navigation.nav

import android.support.v4.app.Fragment
import com.semisonfire.cloudgallery.core.ui.navigation.Navigator

const val EXAMPLE_KEY = "EXAMPLE_KEY"
const val EXAMPLE_CONTAINER_ID = 1

class ExampleNavigator : Navigator() {

    override val key: String
        get() = EXAMPLE_KEY
    override val containerId: Int
        get() = EXAMPLE_CONTAINER_ID

    override fun createFragment(key: String, bundle: Any?): Fragment? {
        return when (key) {
            EXAMPLE_KEY -> null
            else -> null
        }
    }
}