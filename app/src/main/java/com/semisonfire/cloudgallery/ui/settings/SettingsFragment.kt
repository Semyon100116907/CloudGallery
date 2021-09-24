package com.semisonfire.cloudgallery.ui.settings

import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.ui.BaseFragment
import com.semisonfire.cloudgallery.ui.settings.model.SettingsViewModel

interface SettingsView : MvpView<SettingsViewModel>

class SettingsFragment : BaseFragment<SettingsViewModel, SettingsView, SettingsPresenter>() {

    override fun bind(view: View) {
        super.bind(view)
        activity?.let {
            it.findViewById<FloatingActionButton>(R.id.btn_add_new).hide()
            it.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh).isEnabled = false
        }
    }

    override fun layout(): Int {
        return R.layout.fragment_settings
    }
}