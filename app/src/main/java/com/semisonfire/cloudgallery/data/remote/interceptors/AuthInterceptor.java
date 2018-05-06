package com.semisonfire.cloudgallery.data.remote.interceptors;

import android.support.annotation.NonNull;

import com.semisonfire.cloudgallery.data.remote.exceptions.UnauthorizedException;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthInterceptor implements Interceptor {

    private volatile String token;

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request request = chain.request();
        Request modifiedRequest = request;

        if (token != null) {
            modifiedRequest = request.newBuilder()
                    .header("Accept", "application/json")
                    .header("Authorization", "OAuth " + token)
                    .build();
        }

        Response response = chain.proceed(modifiedRequest);

        boolean unauthorized = response.code() == 401;

        if (unauthorized) {
            throw new UnauthorizedException(response.code(), response.message());
        }
        return response;
    }
}
