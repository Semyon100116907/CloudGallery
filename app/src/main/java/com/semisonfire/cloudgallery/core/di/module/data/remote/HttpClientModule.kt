package com.semisonfire.cloudgallery.core.di.module.data.remote

import android.content.Context
import android.net.ConnectivityManager
import com.semisonfire.cloudgallery.BuildConfig
import com.semisonfire.cloudgallery.core.data.remote.interceptors.NetworkConnectionInterceptor
import com.semisonfire.cloudgallery.core.di.AppContext
import com.semisonfire.cloudgallery.core.data.remote.interceptors.AuthInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

//10 Mb cache size
const val DISK_CACHE_SIZE = 10 * 1024 * 1024L

@Module
class HttpClientModule {

  private val cacheName = "response-cache"

  @Provides
  @Singleton
  fun provideHttpClient(
    cache: Cache,
    networkConnectionInterceptor: NetworkConnectionInterceptor,
    loggingInterceptor: HttpLoggingInterceptor,
    authInterceptor: AuthInterceptor
  ): OkHttpClient {

    //Build http client for api
    val builder = getHttpClientBuilder(
      cache,
      networkConnectionInterceptor
    )
    builder.addInterceptor(authInterceptor)

    //Add logging in debug
    if (BuildConfig.DEBUG) {
      builder.addInterceptor(loggingInterceptor)
    }
    return builder.build()
  }

  private fun getHttpClientBuilder(
    cache: Cache,
    networkConnectionInterceptor: NetworkConnectionInterceptor
  ): OkHttpClient.Builder {
    return OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .cache(cache)
      .addInterceptor(networkConnectionInterceptor)
  }

  @Provides
  @Singleton
  fun provideConnectivityManager(@AppContext context: Context): ConnectivityManager {
    return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  }

  @Provides
  @Singleton
  fun provideNetworkConnectionInterceptor(connectivityManager: ConnectivityManager): NetworkConnectionInterceptor {
    return object : NetworkConnectionInterceptor() {
      override val isInternetAvailable: Boolean
        get() = this@HttpClientModule.isInternetAvailable(connectivityManager)
    }
  }

  @Provides
  @Singleton
  fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    return loggingInterceptor
  }

  @Provides
  @Singleton
  fun provideCache(@AppContext context: Context): Cache {
    return Cache(
      File(context.cacheDir, cacheName),
      DISK_CACHE_SIZE
    )
  }

  /** Get current Internet state.  */
  private fun isInternetAvailable(connectivityManager: ConnectivityManager): Boolean {
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
  }
}