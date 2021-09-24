package com.semisonfire.cloudgallery.core.data.remote.interceptors

import com.semisonfire.cloudgallery.core.data.remote.exceptions.InternetUnavailableException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

private const val MAX_STALE = 60 * 60 * 24 //24 hours

abstract class NetworkConnectionInterceptor : Interceptor {

    abstract val isInternetAvailable: Boolean

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!isInternetAvailable) {
            request = request.newBuilder()
                .header(
                    "Cache-Control",
                    "public, only-if-cached, max-stale=$MAX_STALE"
                )
                .build()

            val response = chain.proceed(request)
            if (response.cacheResponse() == null) throw InternetUnavailableException()

            return response
        }
        return chain.proceed(request)
    }
}