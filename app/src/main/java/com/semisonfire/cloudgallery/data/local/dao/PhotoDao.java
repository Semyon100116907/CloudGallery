package com.semisonfire.cloudgallery.data.local.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.semisonfire.cloudgallery.data.model.Photo;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface PhotoDao {

    @Query("SELECT * FROM photo")
    Flowable<List<Photo>> getAllPhotos();

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertPhoto(Photo photo);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void updatePhoto(Photo photo);

    @Delete
    void deletePhoto(Photo photo);

    @Query("DELETE FROM photo")
    void clearAll();
}
