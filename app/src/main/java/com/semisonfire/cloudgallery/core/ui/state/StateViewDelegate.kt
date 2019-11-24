package com.semisonfire.cloudgallery.core.ui.state

import kotlin.properties.Delegates

class StateViewDelegate<T>(
  vararg states: State<T>,
  initialViewState: InitialViewState<T> = HideAllViewState()
) where T : Enum<T> {

  init {
    initialViewState.apply(states)
  }

  private val stateMap = states.associateBy { it.name }

  var currentState: T? by Delegates.observable(null) { _, prevStateName: T?, nextStateName: T? ->
    if (prevStateName == nextStateName) return@observable
    val prevState = stateMap[prevStateName]
    val nextState = stateMap[nextStateName]
    prevState?.run { strategy.onStateExit(this, nextState) }
    nextState?.run { strategy.onStateEnter(this, prevState) }
  }

  fun addState(state: State<T>) {
    if (stateMap is MutableMap) {
      stateMap[state.name] = state
    }
  }
}