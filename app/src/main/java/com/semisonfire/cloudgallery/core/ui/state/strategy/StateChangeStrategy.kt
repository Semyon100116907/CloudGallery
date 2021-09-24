package com.semisonfire.cloudgallery.core.ui.state.strategy

import com.semisonfire.cloudgallery.core.ui.state.State

interface StateChangeStrategy<T : Enum<T>> {
    fun onStateEnter(state: State<T>, prevState: State<T>?)

    fun onStateExit(state: State<T>, nextState: State<T>?)
}