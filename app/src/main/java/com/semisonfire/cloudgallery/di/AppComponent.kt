package com.semisonfire.cloudgallery.di

import android.app.Application
import android.content.SharedPreferences
import com.semisonfire.cloudgallery.core.data.local.LocalDatabase
import com.semisonfire.cloudgallery.di.module.ImageModule
import com.semisonfire.cloudgallery.di.module.NetworkModule
import com.semisonfire.cloudgallery.di.module.StorageModule
import com.squareup.picasso.Picasso
import dagger.BindsInstance
import dagger.Component
import retrofit2.Retrofit
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        StorageModule::class,
        NetworkModule::class,
        ImageModule::class
    ]
)
interface AppComponent {

    fun context(): Application
    fun picasso(): Picasso
    fun localDatabase(): LocalDatabase
    fun preferences(): SharedPreferences
    fun retrofit(): Retrofit

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): AppComponent
    }
}