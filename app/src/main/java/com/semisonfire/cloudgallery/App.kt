package com.semisonfire.cloudgallery

import android.os.Environment
import com.semisonfire.cloudgallery.core.di.AppComponent
import com.semisonfire.cloudgallery.core.di.DaggerAppComponent
import com.semisonfire.cloudgallery.utils.ExternalFileProvider
import com.semisonfire.cloudgallery.utils.FileUtils
import com.squareup.picasso.Picasso
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class App : DaggerApplication() {

    override fun onCreate() {

        //Init external file provider
        val fileProvider = ExternalFileProvider()
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        directory?.let {
            fileProvider.setPrivateDirectory(directory, "CloudGallery")
        }
        FileUtils.fileProvider = fileProvider

        component = DaggerAppComponent.factory().create(this)
        component.inject(this)

        Picasso.setSingletonInstance(component.picasso())
        super.onCreate()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return component
    }

    companion object {

        lateinit var component: AppComponent
    }
}
