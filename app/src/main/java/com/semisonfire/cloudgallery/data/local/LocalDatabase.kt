package com.semisonfire.cloudgallery.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

import com.semisonfire.cloudgallery.data.local.dao.PhotoDao
import com.semisonfire.cloudgallery.data.model.Photo

@Database(
  entities = [
    Photo::class
  ],
  version = LocalDatabase.DATABASE_VERSION,
  exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {

  /*Dao*/
  abstract val photoDao: PhotoDao

  companion object {
    internal const val DATABASE_VERSION = 2
  }

  /*Migrations*/
}