package com.semisonfire.cloudgallery.core.ui.navigation.router

import com.semisonfire.cloudgallery.di.ActivityScope
import com.semisonfire.cloudgallery.core.ui.navigation.Navigator
import javax.inject.Inject

sealed class Command {
    data class Add(val key: String, val bundle: Any?) : Command()
    data class Replace(val key: String, val bundle: Any?) : Command()
    data class Remove(val key: String) : Command()

    object Back : Command()
    object Clear : Command()
}

@ActivityScope
class Router @Inject constructor(private val navigator: Navigator) {

    fun navigateTo(key: String, bundle: Any? = null) {
        val command = Command.Add(key, bundle)
        executeCommand(command)
    }

    fun replaceScreen(key: String, bundle: Any? = null) {
        val command = Command.Replace(key, bundle)
        executeCommand(command)
    }

    fun back() {
        executeCommand(Command.Back)
    }

    fun clear() {
        executeCommand(Command.Clear)
    }

    private fun executeCommand(command: Command) {
        navigator.applyCommand(command)
    }
}