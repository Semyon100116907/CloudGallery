package com.semisonfire.cloudgallery.ui.selectable

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.MenuRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.mvp.MvpViewModel
import com.semisonfire.cloudgallery.core.ui.BaseFragment
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper
import com.semisonfire.cloudgallery.utils.color
import com.semisonfire.cloudgallery.utils.colorResDrawable
import com.semisonfire.cloudgallery.utils.setMenuIconsColor

private const val STATE_SELECTABLE = "STATE_SELECTABLE"

abstract class SelectableFragment<M: MvpViewModel, V : MvpView<M>, P : MvpPresenter<M, V>> : BaseFragment<M, V, P>() {

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

    val secondaryColorRes = if (enabled) R.color.white else R.color.black
    val secondaryColor = context?.color(secondaryColorRes) ?: Color.WHITE

    val activity = activity
    if (activity is AppCompatActivity) {
      activity.supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
      activity.supportActionBar?.setBackgroundDrawable(activity.colorResDrawable(if (enabled) R.color.colorAccent else R.color.white))

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