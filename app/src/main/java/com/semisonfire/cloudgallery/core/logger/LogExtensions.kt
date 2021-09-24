package com.semisonfire.cloudgallery.core.logger

fun Any.debugLog(message: String) {
    Log.d(this.javaClass, message)
}

fun Any.infoLog(message: String, tag: String = "") {
    if (tag.isNotBlank()) {
        Log.log(tag, message, INFO)
    } else {
        Log.i(this.javaClass, message)
    }
}

fun Any.errorLog(message: String) {
    Log.e(this.javaClass, message)
}

fun Throwable.printThrowable() {
    this.printStackTrace()
    Log.log("ERROR", this.message ?: this.javaClass.simpleName + "\n", ERROR)
}