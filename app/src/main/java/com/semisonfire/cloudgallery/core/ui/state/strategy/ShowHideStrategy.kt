package com.semisonfire.cloudgallery.core.ui.state.strategy

import android.view.View
import com.semisonfire.cloudgallery.core.ui.state.State

class ShowHideStrategy<T : Enum<T>> :
    StateChangeStrategy<T> {

    override fun onStateEnter(state: State<T>, prevState: State<T>?) {
        state.listViews.forEach { it.visibility = View.VISIBLE }
    }

    override fun onStateExit(state: State<T>, nextState: State<T>?) {
        state.listViews.forEach { it.visibility = View.GONE }
    }
}

class EnterActionStrategy<T : Enum<T>>(private val action: (View) -> Unit) :
    StateChangeStrategy<T> {

    override fun onStateEnter(state: State<T>, prevState: State<T>?) {
        state.listViews.forEach {
            it.visibility = View.VISIBLE
            action(it)
        }
    }

    override fun onStateExit(state: State<T>, nextState: State<T>?) {
        state.listViews.forEach {
            it.visibility = View.GONE
        }
    }
}