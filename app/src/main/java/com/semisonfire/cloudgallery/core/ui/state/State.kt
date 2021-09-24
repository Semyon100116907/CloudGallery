package com.semisonfire.cloudgallery.core.ui.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.semisonfire.cloudgallery.core.ui.state.strategy.ShowHideStrategy
import com.semisonfire.cloudgallery.core.ui.state.strategy.StateChangeStrategy

data class State<T>(
    val name: T,
    val listViews: List<View> = mutableListOf(),
    val strategy: StateChangeStrategy<T> = ShowHideStrategy()
) where T : Enum<T> {

    constructor(name: T, view: View, strategy: StateChangeStrategy<T> = ShowHideStrategy())
            : this(name, listOf(view), strategy)

    constructor(
        context: Context?,
        name: T,
        @LayoutRes layoutId: Int,
        root: ViewGroup,
        strategy: StateChangeStrategy<T> = ShowHideStrategy()
    ) : this(name = name, strategy = strategy) {
        context?.run {
            val view = LayoutInflater.from(this).inflate(layoutId, root, false)
            root.addView(view)
            (listViews as MutableList).add(view)
        }
    }
}

