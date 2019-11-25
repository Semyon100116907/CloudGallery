package com.semisonfire.cloudgallery.data.remote.interceptors

import com.semisonfire.cloudgallery.data.remote.exceptions.UnauthorizedException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    var modifiedRequest = request
//    if (token != null) {
//      modifiedRequest = request.newBuilder()
//        .header("Accept", "application/json")
//        .header("Authorization", "OAuth $token")
//        .build()
//    }

    val response = chain.proceed(modifiedRequest)
    val unauthorized = response.code() == 401
    if (unauthorized) {
      throw UnauthorizedException(response.code(), response.message())
    }
    return response
  }
}