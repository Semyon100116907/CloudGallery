package com.semisonfire.cloudgallery.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.semisonfire.cloudgallery.BuildConfig;

import java.io.File;

public class ExternalFileProvider extends FileProvider {

    private Context mContext;
    private File mPrivateDirectory;
    private File mPublicDirectory;

    public ExternalFileProvider() {
        super();
    }

    public ExternalFileProvider(Context context) {
        super();
        mContext = context;
    }

    public File getFile(Uri uri) {
        return new File(getPrivateDirectory(), uri.getLastPathSegment());
    }

    public Uri getUri(File file) {
        return getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", file);
    }

    public void setPrivateDirectory(File directory, String name) {
        mPrivateDirectory = new File(directory, name);
    }

    public File getPrivateDirectory() {
        return mPrivateDirectory;
    }

    public File getPublicDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }
}
