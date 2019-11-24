package com.semisonfire.cloudgallery.core.ui.state

import android.view.View

interface InitialViewState<T : Enum<T>> {
  fun apply(states: Array<out State<T>>)
}

class HideAllViewState<T : Enum<T>> :
  InitialViewState<T> {
  override fun apply(states: Array<out State<T>>) {
    states.forEach { state ->
      state.listViews.forEach { it.visibility = View.GONE }
    }
  }
}