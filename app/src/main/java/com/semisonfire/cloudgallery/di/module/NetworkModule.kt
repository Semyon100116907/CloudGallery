package com.semisonfire.cloudgallery.di.module

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.semisonfire.cloudgallery.BuildConfig
import com.semisonfire.cloudgallery.core.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.core.data.remote.interceptors.AuthInterceptor
import com.semisonfire.cloudgallery.core.data.remote.interceptors.NetworkConnectionInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

const val DISK_CACHE_SIZE = 10 * 1024 * 1024L

//App client id
private const val CLIENT_ID = "07bfc4a28ea8403f807fd3dd91dad11f"

//Yandex oauth url
const val OAUTH_URL =
    "https://oauth.yandex.ru/authorize?response_type=token&client_id=$CLIENT_ID"

@Module
class NetworkModule {

    companion object {
        private const val DATE_FORMAT = "dd.MM.yyyy"

        //Rest api url
        private const val BASE_URL = "https://cloud-api.yandex.net/v1/"
    }


    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(httpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): DiskApi {
        return retrofit.create(DiskApi::class.java)
    }

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
    fun provideConnectivityManager(application: Application): ConnectivityManager {
        return application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun provideNetworkConnectionInterceptor(connectivityManager: ConnectivityManager): NetworkConnectionInterceptor {
        return object : NetworkConnectionInterceptor() {
            override val isInternetAvailable: Boolean
                get() = this@NetworkModule.isInternetAvailable(connectivityManager)
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
    fun provideCache(application: Application): Cache {
        return Cache(
            File(application.cacheDir, cacheName),
            DISK_CACHE_SIZE
        )
    }

    /** Get current Internet state.  */
    private fun isInternetAvailable(connectivityManager: ConnectivityManager): Boolean {
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}