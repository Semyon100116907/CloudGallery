package com.semisonfire.cloudgallery.core.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

import com.semisonfire.cloudgallery.core.data.local.dao.PhotoDao
import com.semisonfire.cloudgallery.core.data.model.Photo

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