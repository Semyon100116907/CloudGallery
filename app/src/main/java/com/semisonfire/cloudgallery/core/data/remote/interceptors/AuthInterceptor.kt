package com.semisonfire.cloudgallery.core.data.remote.interceptors

import com.semisonfire.cloudgallery.core.data.remote.auth.Auth
import com.semisonfire.cloudgallery.core.data.remote.auth.AuthRepository
import com.semisonfire.cloudgallery.core.data.remote.exceptions.UnauthorizedException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
  private val authRepository: AuthRepository
) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    var modifiedRequest = request

    val authModel = authRepository.authModel
    if (authModel is Auth.AuthModel) {
      modifiedRequest = request.newBuilder()
        .header("Accept", "application/json")
        .header("Authorization", "OAuth ${authModel.token}")
        .build()
    }

    val response = chain.proceed(modifiedRequest)
    val unauthorized = response.code() == 401
    if (unauthorized) {
      throw UnauthorizedException(response.code(), response.message())
    }
    return response
  }
}