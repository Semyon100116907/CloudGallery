package com.semisonfire.cloudgallery.core.di.module.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.semisonfire.cloudgallery.data.remote.api.DiskApi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(
  includes = [
    HttpClientModule::class,
    PicassoModule::class
  ]
)
class RetrofitModule {

  //Rest api url
  private val BASE_URL = "https://cloud-api.yandex.net" + "/v1/"
  //App client id
  private val CLIENT_ID = "07bfc4a28ea8403f807fd3dd91dad11f"
  //Yandex oauth url
  private val OAUTH_URL = "https://oauth.yandex.ru/authorize?response_type=token&client_id=$CLIENT_ID"

  private val dateFormat = "dd.MM.yyyy"

  @Provides
  @Singleton
  fun provideGson(): Gson {
    return GsonBuilder()
      .setDateFormat(dateFormat)
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
}