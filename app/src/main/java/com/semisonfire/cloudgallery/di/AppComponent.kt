package com.semisonfire.cloudgallery.di

import android.app.Application
import android.content.SharedPreferences
import com.semisonfire.cloudgallery.core.data.local.LocalDatabase
import com.semisonfire.cloudgallery.core.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.core.data.remote.auth.AuthManager
import com.semisonfire.cloudgallery.di.module.ImageModule
import com.semisonfire.cloudgallery.di.module.NetworkModule
import com.semisonfire.cloudgallery.di.module.StorageModule
import com.semisonfire.cloudgallery.ui.disk.data.UploadManager
import com.semisonfire.cloudgallery.ui.main.ui.state.StateViewController
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
    fun stateViewController(): StateViewController

    fun diskApi(): DiskApi

    fun authRepository(): AuthManager
    fun uploadRepository(): UploadManager

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): AppComponent
    }
}