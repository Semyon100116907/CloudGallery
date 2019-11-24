package com.semisonfire.cloudgallery.core.ui.navigation

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.semisonfire.cloudgallery.core.ui.navigation.nav.EXAMPLE_KEY
import com.semisonfire.cloudgallery.core.ui.navigation.nav.ExampleNavigator
import com.semisonfire.cloudgallery.core.ui.navigation.router.Command
import com.semisonfire.cloudgallery.ui.disk.DISK_KEY
import com.semisonfire.cloudgallery.ui.disk.DiskNavigator
import com.semisonfire.cloudgallery.ui.settings.SETTINGS_KEY
import com.semisonfire.cloudgallery.ui.settings.SettingsNavigator
import com.semisonfire.cloudgallery.ui.trash.TRASH_KEY
import com.semisonfire.cloudgallery.ui.trash.TrashNavigator
import java.util.*

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

  private val fragmentStack = Stack<String>()

  var currentKey: String = ""
    private set

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

    fragmentStack.push(key)
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
    val fragment = fragmentManager.findFragmentByTag(key)
    if (fragment != null) {
      fragmentManager
        .beginTransaction()
        .remove(fragment)
        .commit()
    }
  }

  protected fun back() {
    fragmentManager.popBackStack()

    if (!fragmentStack.empty()) {
      currentKey = fragmentStack.pop()
    }
  }

  protected fun clear() {
    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    navigatorMap.clear()
    fragmentStack.clear()

    currentKey = ""
  }

  private fun getContainerIdByKey(key: String): Int {
    return getNavigatorByKey(key).containerId
  }

  override fun createFragment(key: String, bundle: Any?): Fragment? {
    return getNavigatorByKey(key).createFragment(key, bundle)
  }

  private fun getNavigatorByKey(key: String): Navigator {
    currentKey = key

    val navigatorByKey = navigatorMap[key]
    if (navigatorByKey != null) return navigatorByKey

    val navigator = when (key) {
      EXAMPLE_KEY -> ExampleNavigator()
      DISK_KEY -> DiskNavigator()
      TRASH_KEY -> TrashNavigator()
      SETTINGS_KEY -> SettingsNavigator()
      else -> throw RuntimeException("Navigator not found")
    }
    navigatorMap[key] = navigator
    return navigator
  }
}