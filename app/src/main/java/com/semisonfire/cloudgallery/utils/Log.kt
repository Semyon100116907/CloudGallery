package com.semisonfire.cloudgallery.utils

import android.util.Log
import com.semisonfire.cloudgallery.BuildConfig
import com.semisonfire.cloudgallery.utils.Log.d
import com.semisonfire.cloudgallery.utils.Log.e
import com.semisonfire.cloudgallery.utils.Log.i
import com.semisonfire.cloudgallery.utils.Log.log

fun Any.debugLog(message: String) {
    d(this.javaClass, message)
}

fun Any.infoLog(message: String, tag: String = "") {
    if (tag.isNotBlank()) {
        log(tag, message, INFO)
    } else {
        i(this.javaClass, message)
    }
}

fun Any.errorLog(message: String) {
    e(this.javaClass, message)
}

fun Throwable.printThrowable() {
    this.printStackTrace()
//  App.context.fabric {
//    Crashlytics.logException(this)
//  }
    log("ERROR", this.message ?: this.javaClass.simpleName + "\n", ERROR)
}

const val ERROR = 0
const val INFO = 1
const val DEBUG = 2

object Log {

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