package com.semisonfire.cloudgallery.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.semisonfire.cloudgallery.core.data.local.LocalDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class StorageModule {

    companion object {
        private const val DATABASE_NAME = "cloud-gallery.db"
        private const val SHARED_PREFERENCES_NAME = "GALLERY_SHARED_PREF"
    }

    @Provides
    @Singleton
    fun providesLocalDatabase(application: Application): LocalDatabase {
        return Room.databaseBuilder(application, LocalDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    internal fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
}