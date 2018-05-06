package com.semisonfire.cloudgallery.data.local.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.semisonfire.cloudgallery.data.model.Photo;

import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface PhotoDao {

    @Query("SELECT * FROM photo")
    Maybe<List<Photo>> getPhotos();

    @Query("SELECT * FROM photo WHERE upload = 0")
    Maybe<List<Photo>> getUploadingPhotos();

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertPhoto(Photo photo);
}
