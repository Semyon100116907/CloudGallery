package com.semisonfire.cloudgallery.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.semisonfire.cloudgallery.data.local.dao.PhotoDao;
import com.semisonfire.cloudgallery.data.model.Photo;

@Database(entities = Photo.class, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {

    public abstract PhotoDao getPhotoDao();

    private static LocalDatabase instance;

    public static LocalDatabase getInstance(Context context) {

        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), LocalDatabase.class, "cloud-gallery.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}
