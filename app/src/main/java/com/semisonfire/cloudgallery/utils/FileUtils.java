package com.semisonfire.cloudgallery.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {

    private static FileUtils instance;
    private ExternalFileProvider fileProvider;

    private FileUtils() {}

    public static void initInstance(ExternalFileProvider fileProvider) {
        if (instance == null) {
            synchronized (FileUtils.class) {
                if (instance == null) {
                    instance = new FileUtils();
                    instance.fileProvider = fileProvider;
                }
            }
        }
    }

    public static FileUtils getInstance() {
        return instance;
    }

    public Uri createShareFile(Bitmap bitmap) {
        Uri uri = getLocalFileUri("share_image_" + System.currentTimeMillis());
        saveBitmapIntoFile(uri, bitmap);
        return uri;
    }

    public void saveBitmapIntoFile(Uri uri, Bitmap bitmap) {
        saveBitmapIntoFile(getFile(uri), bitmap);
    }

    public void saveBitmapIntoFile(File file, Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get local file
     * @param uri - uri with content:// scheme
     * @return - file
     */
    public File getFile(Uri uri) {
        return fileProvider.getFile(uri);
    }

    /**
     * Get local file uri with today`s date name
     * @return - file uri
     */
    public Uri getLocalFileUri() {
        return getLocalFileUri(new Date());
    }

    /**
     * Get local file uri with {@param date}
     * @param date - file creation date
     * @return - file uri
     */
    public Uri getLocalFileUri(Date date) {
        String time = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(date);
        return getLocalFileUri(time);
    }

    /**
     * Get local file uri
     * @param name - file name
     * @return - file uri
     */
    public Uri getLocalFileUri(String name) {

        File mediaStorageDir = fileProvider.getDirectory();
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdir();
        }
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + (name.endsWith(".png") ? name : name + ".png"));

        if (Build.VERSION.SDK_INT >= 24) {
            return fileProvider.getUri(mediaFile);
        } else {
            return Uri.fromFile(mediaFile);
        }
    }
}
