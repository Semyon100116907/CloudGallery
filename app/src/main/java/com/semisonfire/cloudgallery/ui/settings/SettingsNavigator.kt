package com.semisonfire.cloudgallery.ui.settings

import android.support.v4.app.Fragment
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.ui.navigation.Navigator

const val SETTINGS_KEY = "SETTINGS_KEY"
const val SETTINGS_CONTAINER_ID = R.id.frame_fragment

class SettingsNavigator: Navigator() {
  override val key: String
    get() = SETTINGS_KEY
  override val containerId: Int
    get() = SETTINGS_CONTAINER_ID

  override fun createFragment(key: String, bundle: Any?): Fragment? {
    return when (key) {
      SETTINGS_KEY -> SettingsFragment()
      else -> null
    }
  }
}