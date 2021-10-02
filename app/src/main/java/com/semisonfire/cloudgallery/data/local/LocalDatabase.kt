package com.semisonfire.cloudgallery.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

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