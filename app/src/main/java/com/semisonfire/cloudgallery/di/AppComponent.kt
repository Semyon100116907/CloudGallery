package com.semisonfire.cloudgallery.di

import android.app.Application
import android.content.SharedPreferences
import com.semisonfire.cloudgallery.adapter.di.AdapterModule
import com.semisonfire.cloudgallery.adapter.di.annotation.AdapterScope
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactoryProvider
import com.semisonfire.cloudgallery.core.data.local.LocalDatabase
import com.semisonfire.cloudgallery.core.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.core.data.remote.auth.AuthManager
import com.semisonfire.cloudgallery.di.module.NetworkModule
import com.semisonfire.cloudgallery.di.module.StorageModule
import com.semisonfire.cloudgallery.image.di.ImageHttpClient
import com.semisonfire.cloudgallery.image.di.ImageModule
import com.semisonfire.cloudgallery.navigation.di.annotation.NavigationScope
import com.semisonfire.cloudgallery.navigation.di.module.NavigationModule
import com.semisonfire.cloudgallery.navigation.router.Router
import com.semisonfire.cloudgallery.ui.disk.data.UploadManager
import com.semisonfire.cloudgallery.ui.main.ui.state.StateViewController
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Singleton
@NavigationScope
@AdapterScope
@Component(
    modules = [
        StorageModule::class,
        NetworkModule::class,
        ImageModule::class,
        NavigationModule::class,
        AdapterModule::class
    ]
)
interface AppComponent {

    @ImageHttpClient
    fun imageHttpClient(): OkHttpClient

    fun context(): Application
    fun localDatabase(): LocalDatabase
    fun preferences(): SharedPreferences
    fun retrofit(): Retrofit
    fun stateViewController(): StateViewController

    fun adapterFactoryProvider(): AdapterFactoryProvider

    fun router(): Router

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