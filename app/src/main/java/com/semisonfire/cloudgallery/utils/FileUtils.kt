package com.semisonfire.cloudgallery.utils

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import com.semisonfire.cloudgallery.utils.DateUtils.getDateString
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

object FileUtils {
  lateinit var fileProvider: ExternalFileProvider
  /**
   * Create temp file to share.
   */
  fun createShareFile(bitmap: Bitmap): Uri {
    val uri = getLocalFileUri("share_image_" + System.currentTimeMillis())
    saveFile(uri, bitmap)
    return uri
  }

  fun savePublicFile(bitmap: Bitmap, fileName: String): String {
    val publicDirectory = File(
      fileProvider.publicDirectory,
      "CloudGallery"
    )
    if (!publicDirectory.exists()) {
      publicDirectory.mkdir()
    }
    val index = fileName.lastIndexOf('.')
    val name = fileName.substring(0, index)
    val extension = fileName.substring(index)
    val file = createPublicFile(publicDirectory, name, extension, 0)
    saveFile(file, bitmap)
    return file.absolutePath
  }

  /**
   * Create file in public external directory.
   */
  private fun createPublicFile(
    directory: File,
    name: String,
    extension: String,
    counter: Int
  ): File {
    val newName: String = if (counter != 0) "$name($counter)" else name
    val file = File(directory, newName + extension)
    if (file.exists()) {
      return createPublicFile(directory, name, extension, counter + 1)
    }
    try {
      file.createNewFile()
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return file
  }

  fun saveFile(uri: Uri, bitmap: Bitmap) {
    saveFile(getFile(uri), bitmap)
  }

  fun saveFile(file: File, bitmap: Bitmap) {
    var fileOutputStream: FileOutputStream? = null
    try {
      fileOutputStream = FileOutputStream(file)
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    } finally {
      try {
        fileOutputStream?.close()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }

  /**
   * Get local file
   */
  fun getFile(uri: Uri): File {
    return fileProvider.getFile(uri)
  }

  /**
   * Get local file uri with today`s date name
   */
  val localFileUri: Uri
    get() = getLocalFileUri(Date())

  /**
   * Get local file uri with {@param date}
   */
  fun getLocalFileUri(date: Date?): Uri {
    val time = "IMG_" + getDateString(
      date,
      DateUtils.DEFAULT_FORMAT
    )
    return getLocalFileUri(time)
  }

  /**
   * Get local file uri
   */
  private fun getLocalFileUri(name: String): Uri {
    val mediaStorageDir = fileProvider.privateDirectory
    if (!mediaStorageDir.exists()) {
      mediaStorageDir.mkdir()
    }
    val mediaFile = File(
      mediaStorageDir.path + File.separator
        + if (name.endsWith(".png")) name else "$name.png"
    )
    return if (Build.VERSION.SDK_INT >= 24) {
      fileProvider.getUri(mediaFile)
    } else {
      Uri.fromFile(mediaFile)
    }
  }
}