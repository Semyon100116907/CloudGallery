package com.semisonfire.cloudgallery.core.di.module.data.remote

import android.content.Context
import com.semisonfire.cloudgallery.BuildConfig
import com.semisonfire.cloudgallery.data.remote.interceptors.NetworkConnectionInterceptor
import com.semisonfire.cloudgallery.core.di.AppContext
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class PicassoModule {
  private val cacheName: String = "picasso-cache"

  @Provides
  @Singleton
  fun providePicasso(
    @AppContext context: Context,
    @Named("PICASSO") picassoClient: OkHttpClient
  ): Picasso {

    val picasso = Picasso.Builder(context)
      .indicatorsEnabled(BuildConfig.DEBUG)
      .loggingEnabled(BuildConfig.DEBUG)
      .downloader(OkHttp3Downloader(picassoClient))
      .listener { pic, uri, exception -> exception.printStackTrace() }
      .build()

    Picasso.setSingletonInstance(picasso)
    return picasso
  }

  @Provides
  @Singleton
  @Named("PICASSO")
  fun providePicassoClient(
    @Named("PICASSO") cache: Cache,
    networkConnectionInterceptor: NetworkConnectionInterceptor
  ): OkHttpClient {
    val picassoClient = OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .cache(cache)
      .addInterceptor(networkConnectionInterceptor)

    return picassoClient.build()
  }

  @Provides
  @Singleton
  @Named("PICASSO")
  fun provideCache(@AppContext context: Context): Cache {
    return Cache(
      File(context.cacheDir, cacheName),
      DISK_CACHE_SIZE
    )
  }
}