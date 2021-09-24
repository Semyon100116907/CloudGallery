package com.semisonfire.cloudgallery.di.provider

import android.content.Context

interface ComponentProvider<T> {

    fun component(): T?
}

inline fun <reified T> Context.provideComponent(): T {

    var component: T? = null
    if (this is ComponentProvider<*>) {
        component = this.component() as? T
    }

    if (component == null) {
        component = (applicationContext as? ComponentProvider<T>)?.component()
    }

    return component ?: throw IllegalArgumentException("Component not found")
}