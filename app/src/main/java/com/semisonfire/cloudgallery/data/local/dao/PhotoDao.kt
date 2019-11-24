package com.semisonfire.cloudgallery.data.local.dao

import android.arch.persistence.room.*
import com.semisonfire.cloudgallery.data.model.Photo
import io.reactivex.Single

@Dao
interface PhotoDao {

  @get:Query("SELECT * FROM photo")
  val photos: Single<List<Photo>>

  @get:Query("SELECT * FROM photo WHERE upload = 0")
  val uploadingPhotos: Single<List<Photo>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertPhoto(photo: Photo?): Long

  @Delete
  fun deletePhoto(photo: Photo?)
}