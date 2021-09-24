package com.semisonfire.cloudgallery.core.di.module.data.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPreferencesModule {

    @Provides
    @Singleton
    internal fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val SHARED_PREFERENCES_NAME = "GALLERY_SHARED_PREF"
    }
}