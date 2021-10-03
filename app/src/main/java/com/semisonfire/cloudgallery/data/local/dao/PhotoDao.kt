package com.semisonfire.cloudgallery.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.semisonfire.cloudgallery.data.local.entity.PhotoEntity
import io.reactivex.Single

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photo")
    fun getPhotos(): Single<List<PhotoEntity>>

    @Query("SELECT * FROM photo WHERE photo.upload = 0")
    fun getUploadingPhotos(): Single<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(photo: PhotoEntity)

    @Query("DELETE FROM photo WHERE photo.id=:id")
    fun deleteById(id: String)

    @Delete
    fun delete(photo: PhotoEntity)
}