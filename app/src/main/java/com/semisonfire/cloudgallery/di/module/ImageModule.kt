package com.semisonfire.cloudgallery.di.module

import android.app.Application
import com.semisonfire.cloudgallery.BuildConfig
import com.semisonfire.cloudgallery.core.logger.printThrowable
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ImageModule {
    private val cacheName: String = "picasso-cache"

    @Provides
    @Singleton
    fun providePicasso(
        application: Application,
        client: OkHttpClient
    ): Picasso {
        return Picasso.Builder(application)
            .indicatorsEnabled(BuildConfig.DEBUG)
            .loggingEnabled(BuildConfig.DEBUG)
            .downloader(OkHttp3Downloader(provideClient(application, client)))
            .listener { _, _, exception -> exception.printThrowable() }
            .build()
    }

    private fun provideClient(application: Application, client: OkHttpClient): OkHttpClient {
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
            DISK_CACHE_SIZE
        )
    }
}