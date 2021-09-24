package com.semisonfire.cloudgallery.di.module

import com.semisonfire.cloudgallery.core.data.remote.interceptors.AuthInterceptor
import com.semisonfire.cloudgallery.core.data.remote.interceptors.NetworkConnectionInterceptor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DebugInterceptors

@Module
internal abstract class NetworkInterceptorsModule {

    companion object {

        @Provides
        @IntoSet
        @DebugInterceptors
        @Singleton
        fun provideLoggingInterceptor(): Interceptor {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            return loggingInterceptor
        }
    }

    @Binds
    @IntoSet
    abstract fun bindsNetworkInterceptor(impl: NetworkConnectionInterceptor): Interceptor

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindsAuthInterceptor(impl: AuthInterceptor): Interceptor
}