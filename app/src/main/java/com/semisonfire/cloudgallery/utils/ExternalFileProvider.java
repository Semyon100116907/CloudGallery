package com.semisonfire.cloudgallery.utils;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.semisonfire.cloudgallery.BuildConfig;

import java.io.File;

public class ExternalFileProvider extends FileProvider {

    private Context mContext;
    private File mDirectory;

    public ExternalFileProvider() {
        super();
    }

    public ExternalFileProvider(Context context) {
        super();
        mContext = context;
    }

    public File getFile(Uri uri) {
        return new File(getDirectory(), uri.getLastPathSegment());
    }

    public Uri getUri(File file) {
        return getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", file);
    }

    public Uri getUri(String authority, File file) {
        return getUriForFile(mContext, authority, file);
    }

    public void setDirectory(File directory, String name) {
        mDirectory = new File(directory, name);
    }

    public File getDirectory() {
        return mDirectory;
    }
}
