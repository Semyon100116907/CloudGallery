package com.semisonfire.cloudgallery.di

import android.content.Context
import com.semisonfire.cloudgallery.App
import com.semisonfire.cloudgallery.di.builder.ActivityProvider
import com.semisonfire.cloudgallery.di.module.AppModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Qualifier
import javax.inject.Scope
import javax.inject.Singleton

@Qualifier
annotation class AppContext

@Qualifier
annotation class ActivityContext

@Retention(AnnotationRetention.RUNTIME)
@Scope
annotation class ActivityScope

@Singleton
@Component(
  modules = [
    AndroidInjectionModule::class,
    ActivityProvider::class,
    AppModule::class
  ]
)
interface AppComponent : AndroidInjector<App> {
  @AppContext
  fun context(): Context
}