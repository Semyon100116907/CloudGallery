package com.semisonfire.cloudgallery.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class DiskPreferences {

    private static final String PACKAGE = DiskPreferences.class.getPackage().getName();
    private static final String GALLERY_SHARED_PREF = PACKAGE + ".DISK_SHARED_PREF";
    private static final String PREF_TOKEN = PACKAGE + ".TOKEN";

    private SharedPreferences mSharedPreferences;

    public DiskPreferences(Context context) {
        this.mSharedPreferences = context.getSharedPreferences(GALLERY_SHARED_PREF, Context.MODE_PRIVATE);
    }

    public void setPrefToken(String token) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(PREF_TOKEN, token);
        mEditor.apply();
    }

    public String getPrefToken() {
        return mSharedPreferences.getString(PREF_TOKEN, null);
    }
}
