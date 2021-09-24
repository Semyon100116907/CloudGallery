package com.semisonfire.cloudgallery

import android.app.Application
import android.os.Environment
import com.semisonfire.cloudgallery.di.AppComponent
import com.semisonfire.cloudgallery.di.DaggerAppComponent
import com.semisonfire.cloudgallery.ui.di.ComponentProvider
import com.semisonfire.cloudgallery.utils.ExternalFileProvider
import com.semisonfire.cloudgallery.utils.FileUtils
import com.squareup.picasso.Picasso

class App : Application(), ComponentProvider<AppComponent> {

    lateinit var component: AppComponent

    override fun onCreate() {

        //Init external file provider
        val fileProvider = ExternalFileProvider()
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        directory?.let {
            fileProvider.setPrivateDirectory(directory, "CloudGallery")
        }
        FileUtils.application = this
        FileUtils.fileProvider = fileProvider

        component = DaggerAppComponent.factory().create(this)

        Picasso.setSingletonInstance(component.picasso())
        super.onCreate()
    }

    override fun component(): AppComponent {
        return component
    }
}
