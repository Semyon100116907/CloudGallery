package com.semisonfire.cloudgallery.core.ui.navigation

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.semisonfire.cloudgallery.core.ui.navigation.nav.EXAMPLE_KEY
import com.semisonfire.cloudgallery.core.ui.navigation.nav.ExampleNavigator
import com.semisonfire.cloudgallery.core.ui.navigation.router.Command

private const val NONE_CONTAINER_ID = -1

abstract class Navigator {

    abstract val containerId: Int
    abstract val key: String
    abstract fun createFragment(key: String, bundle: Any? = null): Fragment?
    open fun applyCommand(command: Command) {}
}

class NavigatorImpl(private val fragmentManager: FragmentManager) : Navigator() {

    override val containerId: Int
        get() = NONE_CONTAINER_ID
    override val key: String
        get() = ""

    private val navigatorMap: MutableMap<String, Navigator> = mutableMapOf()

    override fun applyCommand(command: Command) {
        super.applyCommand(command)
        when (command) {
            is Command.Add -> add(command.key, command.bundle)
            is Command.Replace -> replace(command.key, command.bundle)
            is Command.Remove -> remove(command.key)
            is Command.Back -> back()
            is Command.Clear -> clear()
        }
    }

    protected fun add(key: String, bundle: Any?) {
        val containerId = getContainerIdByKey(key)
        if (containerId == NONE_CONTAINER_ID)
            return

        val fragment = createFragment(key, bundle) ?: return
        fragmentManager
            .beginTransaction()
            .add(containerId, fragment, key)
            .addToBackStack(key)
            .commit()
    }

    protected fun replace(key: String, bundle: Any?) {
        val containerId = getContainerIdByKey(key)
        if (containerId == NONE_CONTAINER_ID)
            return

        val fragment = createFragment(key, bundle) ?: return
        fragmentManager
            .beginTransaction()
            .replace(containerId, fragment, key)
            .commit()
    }

    protected fun remove(key: String) {
        val fragment = createFragment(key) ?: return
        fragmentManager
            .beginTransaction()
            .remove(fragment)
            .commit()
    }

    protected fun back() {
        fragmentManager.popBackStack()
    }

    protected fun clear() {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        navigatorMap.clear()
    }

    private fun getContainerIdByKey(key: String): Int {
        return getNavigatorByKey(key).containerId
    }

    override fun createFragment(key: String, bundle: Any?): Fragment? {
        return getNavigatorByKey(key).createFragment(key, bundle)
    }

    private fun getNavigatorByKey(key: String): Navigator {
        val navigatorByKey = navigatorMap[key]
        if (navigatorByKey != null)
            return navigatorByKey

        val navigator = when (key) {
            EXAMPLE_KEY -> ExampleNavigator()
            else -> throw RuntimeException("Navigator not found")
        }
        navigatorMap[key] = navigator
        return navigator
    }
}