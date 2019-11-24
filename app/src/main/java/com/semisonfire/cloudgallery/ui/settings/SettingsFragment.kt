package com.semisonfire.cloudgallery.ui.settings

import android.view.View
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.ui.BaseFragment

interface SettingsView : MvpView

class SettingsFragment : BaseFragment<SettingsView, SettingsPresenter>() {

  override fun bind(view: View) {
    super.bind(view)

//    floatButton.hide()
//    swipeRefreshLayout.isEnabled = false
  }

  override fun layout(): Int {
    return R.layout.fragment_settings
  }
}