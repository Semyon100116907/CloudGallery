package com.semisonfire.cloudgallery.core.logger

import android.util.Log
import com.semisonfire.cloudgallery.BuildConfig

internal const val ERROR = 0
internal const val INFO = 1
internal const val DEBUG = 2

internal object Log {

    private val info = { tag: String, message: String -> Log.i(tag, message) }
    private val debug = { tag: String, message: String -> Log.d(tag, message) }
    private val error = { tag: String, message: String -> Log.e(tag, message) }

    fun log(tag: String, message: String, type: Int) {
        when (type) {
            ERROR -> error.invoke(tag, message)
            INFO -> info.invoke(tag, message)
            DEBUG -> debug.invoke(tag, message)
            else -> debug.invoke(tag, message)
        }
    }

    private fun log(
        clazz: Class<Any>,
        message: String,
        type: Int
    ) {
        if (BuildConfig.DEBUG) {
            log(clazz.simpleName + " " + Thread.currentThread(), message, type)
        }
    }

    fun d(clazz: Class<Any>, message: String) {
        log(clazz, message, DEBUG)
    }

    fun i(clazz: Class<Any>, message: String) {
        log(clazz, message, INFO)
    }

    fun e(clazz: Class<Any>, message: String) {
        log(clazz, message, ERROR)
    }
}