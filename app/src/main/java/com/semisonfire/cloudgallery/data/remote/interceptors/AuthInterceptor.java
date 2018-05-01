package com.semisonfire.cloudgallery.data.remote.interceptors;

import android.support.annotation.NonNull;
import android.util.Log;

import com.semisonfire.cloudgallery.data.remote.exceptions.UnauthorizedException;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private String token;

    public AuthInterceptor(String token) {
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
        //Log.e("RESPONSE", "authenticate: " + response.message());
        return response;
    }
}
