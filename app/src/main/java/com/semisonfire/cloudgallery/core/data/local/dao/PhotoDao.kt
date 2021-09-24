package com.semisonfire.cloudgallery.core.data.local.dao

import androidx.room.*
import com.semisonfire.cloudgallery.core.data.model.Photo
import io.reactivex.Single

@Dao
interface PhotoDao {

    @get:Query("SELECT * FROM photo")
    val photos: Single<List<Photo>>

    @get:Query("SELECT * FROM photo WHERE upload = 0")
    val uploadingPhotos: Single<List<Photo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoto(photo: Photo)

    @Delete
    fun deletePhoto(photo: Photo)
}