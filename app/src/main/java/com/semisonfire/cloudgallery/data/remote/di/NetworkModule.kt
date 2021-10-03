package com.semisonfire.cloudgallery.data.remote.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.semisonfire.cloudgallery.BuildConfig
import com.semisonfire.cloudgallery.data.remote.api.DiskApi
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

//App client id
private const val CLIENT_ID = "07bfc4a28ea8403f807fd3dd91dad11f"

//Yandex oauth url
const val OAUTH_URL =
    "https://oauth.yandex.ru/authorize?response_type=token&client_id=$CLIENT_ID"

@Module(
    includes = [NetworkInterceptorsModule::class]
)
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
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
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
        application: Application,
        interceptors: Set<@JvmSuppressWildcards Interceptor>,
        @DebugInterceptors debugInterceptors: Set<@JvmSuppressWildcards Interceptor>,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(
                Cache(
                    File(application.cacheDir, cacheName),
                    10 * 1024 * 1024L
                )
            )
            .apply {
                interceptors.forEach { addInterceptor(it) }

                if (BuildConfig.DEBUG) {
                    debugInterceptors.forEach { addInterceptor(it) }
                }
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(application: Application): ConnectivityManager {
        return application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}