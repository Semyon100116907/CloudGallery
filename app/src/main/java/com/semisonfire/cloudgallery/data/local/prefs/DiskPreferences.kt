package com.semisonfire.cloudgallery.data.local.prefs

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

private const val PREF_TOKEN = "TOKEN"

@Singleton
class DiskPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {

  var prefToken: String?
    get() = sharedPreferences.getString(PREF_TOKEN, null)
    set(token) {
      val editor = sharedPreferences.edit()
      editor.putString(PREF_TOKEN, token)
      editor.apply()
    }
}
