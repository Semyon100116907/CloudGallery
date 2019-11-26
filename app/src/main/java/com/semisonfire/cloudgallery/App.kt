package com.semisonfire.cloudgallery

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Environment
import android.support.multidex.MultiDex
import com.semisonfire.cloudgallery.core.di.AppComponent
import com.semisonfire.cloudgallery.core.di.DaggerAppComponent
import com.semisonfire.cloudgallery.core.di.module.ContextModule
import com.semisonfire.cloudgallery.core.di.module.data.local.RoomModule
import com.semisonfire.cloudgallery.utils.ExternalFileProvider
import com.semisonfire.cloudgallery.utils.FileUtils
import com.squareup.picasso.Picasso
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import javax.inject.Inject

class App : Application(), HasActivityInjector {
  @Inject
  internal lateinit var activityInjector: DispatchingAndroidInjector<Activity>

  override fun activityInjector(): AndroidInjector<Activity> {
    return activityInjector
  }

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    MultiDex.install(base)
  }

  override fun onCreate() {
    super.onCreate()

    //Init external file provider
    val fileProvider = ExternalFileProvider()
    val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    directory?.let {
      fileProvider.setPrivateDirectory(directory, "CloudGallery")
    }
    FileUtils.fileProvider = fileProvider

    component = DaggerAppComponent.builder()
      .contextModule(ContextModule(this))
      .roomModule(RoomModule(this))
      .build()
    component.inject(this)

    Picasso.setSingletonInstance(component.picasso())
  }

  companion object {

    lateinit var component: AppComponent

    val background = lazy {
      Schedulers.from(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
    }

    val foreground = lazy {
      AndroidSchedulers.mainThread()
    }
  }
}
