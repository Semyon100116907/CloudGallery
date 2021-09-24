package com.semisonfire.cloudgallery.core.di

import android.app.Application
import com.semisonfire.cloudgallery.App
import com.semisonfire.cloudgallery.core.di.builder.ActivityProvider
import com.semisonfire.cloudgallery.core.di.module.AppModule
import com.squareup.picasso.Picasso
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Scope
import javax.inject.Singleton

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

    fun context(): Application
    fun picasso(): Picasso

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): AppComponent
    }
}