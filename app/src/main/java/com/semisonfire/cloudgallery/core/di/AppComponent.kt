package com.semisonfire.cloudgallery.core.di

import android.content.Context
import com.semisonfire.cloudgallery.App
import com.semisonfire.cloudgallery.core.di.builder.ActivityProvider
import com.semisonfire.cloudgallery.core.di.module.AppModule
import com.squareup.picasso.Picasso
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

@Retention(AnnotationRetention.RUNTIME)
@Scope
annotation class FragmentScope

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

  fun picasso(): Picasso
}