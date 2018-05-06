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

    private FileUtils() {
    }

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

    /** Create temp file to share. */
    public Uri createShareFile(Bitmap bitmap) {
        Uri uri = getLocalFileUri("share_image_" + System.currentTimeMillis());
        saveFile(uri, bitmap);
        return uri;
    }

    public String savePublicFile(Bitmap bitmap, String fileName) {
        File publicDirectory = new File(fileProvider.getPublicDirectory(), "CloudGallery");
        if (!publicDirectory.exists()) {
            publicDirectory.mkdir();
        }
        int index = fileName.lastIndexOf('.');
        String name = fileName.substring(0, index);
        String extension = fileName.substring(index);
        File file = createPublicFile(publicDirectory, name, extension, 0);
        saveFile(file, bitmap);
        return file.getAbsolutePath();
    }

    /** Create file in public external directory. */
    private File createPublicFile(File directory, String name, String extension, int counter) {

        String newName = name;
        if (counter != 0) {
            newName = name + "(" + String.valueOf(counter) + ")";
        }

        File file = new File(directory, newName + extension);
        if (file.exists()) {
            counter++;
            return createPublicFile(directory, name, extension, counter);
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void saveFile(Uri uri, Bitmap bitmap) {
        saveFile(getFile(uri), bitmap);
    }

    public void saveFile(File file, Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
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

    /** Get local file */
    public File getFile(Uri uri) {
        return fileProvider.getFile(uri);
    }

    /** Get local file uri with today`s date name */
    public Uri getLocalFileUri() {
        return getLocalFileUri(new Date());
    }

    /** Get local file uri with {@param date} */
    public Uri getLocalFileUri(Date date) {
        String time = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(date);
        return getLocalFileUri(time);
    }

    /** Get local file uri */
    public Uri getLocalFileUri(String name) {

        File mediaStorageDir = fileProvider.getPrivateDirectory();
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
