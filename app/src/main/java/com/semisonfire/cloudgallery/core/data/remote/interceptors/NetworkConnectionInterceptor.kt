package com.semisonfire.cloudgallery.core.data.remote.interceptors

import android.net.ConnectivityManager
import com.semisonfire.cloudgallery.core.data.remote.exceptions.InternetUnavailableException
import java.io.IOException
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

private const val MAX_STALE = 60 * 60 * 24 //24 hours

class NetworkConnectionInterceptor @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : Interceptor {

    private val isInternetAvailable: Boolean
        get() {
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }

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
            if (response.cacheResponse == null) throw InternetUnavailableException()

            return response
        }
        return chain.proceed(request)
    }
}