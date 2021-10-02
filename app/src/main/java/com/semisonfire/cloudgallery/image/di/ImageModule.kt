package com.semisonfire.cloudgallery.image.di

import android.app.Application
import dagger.Module
import dagger.Provides
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.Cache
import okhttp3.OkHttpClient

@Module
class ImageModule {
    private val cacheName: String = "images-cache"

    @Provides
    @Singleton
    @ImageHttpClient
    fun provideClient(
        application: Application,
        client: OkHttpClient
    ): OkHttpClient {

        return client.newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(provideCache(application))
            .build()
    }

    private fun provideCache(application: Application): Cache {
        return Cache(
            File(application.cacheDir, cacheName),
            10 * 1024 * 1024L
        )
    }
}