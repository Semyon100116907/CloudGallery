package com.semisonfire.cloudgallery.data.remote.interceptors;

import android.support.annotation.NonNull;

import com.semisonfire.cloudgallery.data.remote.exceptions.InternetUnavailableException;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public abstract class NetworkConnectionInterceptor implements Interceptor {

    public abstract boolean isInternetAvailable();

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request request = chain.request();

        if (!isInternetAvailable()) {
            request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24)
                    .build();

            Response response = chain.proceed(request);
            if (response.cacheResponse() == null) {
                throw new InternetUnavailableException();
            }
            return response;
        }

        return chain.proceed(request);
    }
}
