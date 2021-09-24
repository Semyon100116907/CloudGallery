package com.semisonfire.cloudgallery.utils

import android.net.Uri
import android.os.Environment
import android.support.v4.content.FileProvider
import com.semisonfire.cloudgallery.App
import com.semisonfire.cloudgallery.BuildConfig
import java.io.File

class ExternalFileProvider : FileProvider() {

    lateinit var privateDirectory: File
        private set

    val publicDirectory: File
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

    fun getFile(uri: Uri): File {
        return File(privateDirectory, uri.lastPathSegment ?: "")
    }

    fun getUri(file: File): Uri {
        return getUriForFile(
            App.component.context(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
    }

    fun setPrivateDirectory(directory: File, name: String) {
        privateDirectory = File(directory, name)
    }

}