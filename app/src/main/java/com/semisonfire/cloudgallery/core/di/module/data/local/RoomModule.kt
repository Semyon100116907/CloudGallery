package com.semisonfire.cloudgallery.core.di.module.data.local

import android.app.Application
import android.arch.persistence.room.Room
import com.semisonfire.cloudgallery.core.data.local.LocalDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class RoomModule {

    @Provides
    @Singleton
    fun providesLocalDatabase(application: Application): LocalDatabase {
        return Room.databaseBuilder(application, LocalDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    companion object {
        private const val DATABASE_NAME = "cloud-gallery.db"
    }
}