package com.semisonfire.cloudgallery.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.semisonfire.cloudgallery.R

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            it.findViewById<FloatingActionButton>(R.id.btn_add_new).hide()
            it.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh).isEnabled = false
        }
    }
}