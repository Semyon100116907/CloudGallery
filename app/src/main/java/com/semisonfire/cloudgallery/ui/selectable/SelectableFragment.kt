package com.semisonfire.cloudgallery.ui.selectable

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.ui.ContentFragment
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper
import com.semisonfire.cloudgallery.utils.color
import com.semisonfire.cloudgallery.utils.colorDrawable
import com.semisonfire.cloudgallery.utils.setMenuIconsColor
import com.semisonfire.cloudgallery.utils.themeColor

private const val STATE_SELECTABLE = "STATE_SELECTABLE"

abstract class SelectableFragment : ContentFragment() {

    protected var menu: Menu? = null
    protected var isSelectable = false

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            isSelectable = getBoolean(STATE_SELECTABLE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean(STATE_SELECTABLE, isSelectable)
        }
    }

    @MenuRes
    open fun menuRes(): Int {
        return -1
    }

    override fun bind(view: View) {
        super.bind(view)
        val hasMenu = menuRes() != -1
        if (hasMenu) {
            val activity = activity
            if (activity is AppCompatActivity) {
                activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
            }
            setHasOptionsMenu(hasMenu)
        }
    }

    @CallSuper
    open fun setEnabledSelection(enabled: Boolean) {
        SelectableHelper.setMultipleSelection(enabled)

        val secondaryColor = if (enabled)
            requireContext().color(R.color.color_white) else requireContext().themeColor(R.attr.colorTextPrimary)

        val activity = activity
        if (activity is AppCompatActivity) {
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
            activity.supportActionBar?.setBackgroundDrawable(
                activity.colorDrawable(
                    if (enabled) activity.color(R.color.color_blue_500) else activity.themeColor(R.attr.colorPrimary)
                )
            )

            activity.findViewById<Toolbar>(R.id.toolbar).setTitleTextColor(secondaryColor)
        }

        menu?.let {
            setMenuIconsColor(it, secondaryColor)
        }

        isSelectable = enabled
    }

    @CallSuper
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(menuRes(), menu)
        this.menu = menu

        setEnabledSelection(isSelectable)
    }
}