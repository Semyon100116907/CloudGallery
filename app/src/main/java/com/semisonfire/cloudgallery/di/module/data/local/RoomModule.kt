package com.semisonfire.cloudgallery.di.module.data.local

import android.arch.persistence.room.Room
import android.content.Context
import com.semisonfire.cloudgallery.data.local.LocalDatabase
import com.semisonfire.cloudgallery.di.AppContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule(@AppContext context: Context) {

  private val localDatabase: LocalDatabase

  init {
    localDatabase = Room.databaseBuilder(
      context,
      LocalDatabase::class.java,
      DATABASE_NAME
    )
      .fallbackToDestructiveMigration()
      .build()
  }

  @Provides
  @Singleton
  internal fun providesLocalDatabase(): LocalDatabase {
    return localDatabase
  }

  companion object {
    private const val DATABASE_NAME = "cloud-gallery.db"
  }

}