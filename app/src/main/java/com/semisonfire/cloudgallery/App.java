package com.semisonfire.cloudgallery;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.utils.ExternalFileProvider;
import com.semisonfire.cloudgallery.utils.FileUtils;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        //Init client
        DiskClient.initInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

        //Init external file provider
        ExternalFileProvider fileProvider = new ExternalFileProvider(this);
        fileProvider.setPrivateDirectory(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CloudGallery");
        FileUtils.initInstance(fileProvider);

        //Create api
        DiskPreferences diskPreferences = new DiskPreferences(getApplicationContext());
        DiskClient.getInstance().createApi(this, diskPreferences.getPrefToken());
        DiskClient.getInstance().createPicasso(this);
    }
}
