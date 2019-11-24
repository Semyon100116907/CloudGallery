package com.semisonfire.cloudgallery.di.module.data.local

import android.content.Context
import android.content.SharedPreferences
import com.semisonfire.cloudgallery.di.AppContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPreferencesModule {

  @Provides
  @Singleton
  internal fun provideSharedPreferences(@AppContext context: Context): SharedPreferences {
    return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
  }

  companion object {
    private const val SHARED_PREFERENCES_NAME = "GALLERY_SHARED_PREF"
  }
}