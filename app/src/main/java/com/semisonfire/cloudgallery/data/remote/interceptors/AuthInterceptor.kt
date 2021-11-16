package com.semisonfire.cloudgallery.data.remote.interceptors

import com.semisonfire.cloudgallery.data.remote.auth.AuthManager
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager
) : Interceptor {

    companion object {
        private const val AUTH_HEADER_KEY = "Authorization"
        private const val AUTH_HEADER_VALUE = "OAuth %s"

        private const val ACCEPT_HEADER_KEY = "Accept"
        private const val ACCEPT_HEADER_VALUE = "application/json"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var requestBuilder = request.newBuilder()

        val authModel = authManager.authModel
        if (authModel.token.isNotBlank()) {
            requestBuilder = requestBuilder.header(
                AUTH_HEADER_KEY,
                AUTH_HEADER_VALUE.format(authModel.token)
            )
        }

        val response = chain.proceed(
            requestBuilder
                .header(ACCEPT_HEADER_KEY, ACCEPT_HEADER_VALUE)
                .build()
        )

        val unauthorized = response.code == 401
        if (unauthorized) {
            authManager.logout()
            return response
        }
        return response
    }
}