package com.semisonfire.cloudgallery.data.remote.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.semisonfire.cloudgallery.BuildConfig;
import com.semisonfire.cloudgallery.data.remote.interceptors.AuthInterceptor;
import com.semisonfire.cloudgallery.data.remote.interceptors.NetworkConnectionInterceptor;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DiskClient {

    //Rest api url
    public static final String BASE_URL = "https://cloud-api.yandex.net" + "/v1/";
    //App client id
    public static final String CLIENT_ID = "07bfc4a28ea8403f807fd3dd91dad11f";
    //Yandex oauth url
    public static final String OAUTH_URL = "https://oauth.yandex.ru/authorize?response_type=token&client_id=" + CLIENT_ID;
    //10 Mb cache size
    public static final int DISK_CACHE_SIZE = 10 * 1024 * 1024;

    private static DiskClient instance;
    private static ConnectivityManager connectivityManager;
    private static DiskApi api;
    private static String token;

    private AuthInterceptor authInterceptor;

    public AuthInterceptor getAuthInterceptor() {
        return authInterceptor;
    }

    private DiskClient() {
    }

    public static void initInstance(ConnectivityManager connectivityManager) {
        if (instance == null) {
            synchronized (DiskClient.class) {
                if (instance == null) {
                    instance = new DiskClient();
                }
            }
        }
        DiskClient.connectivityManager = connectivityManager;
    }

    public static DiskClient getInstance() {
        return instance;
    }

    public static String getToken() {
        return token;
    }

    public static DiskApi getApi() {
        return api;
    }

    /** Create api {@link DiskApi} instance. */
    public void createApi(Context context, String token) {

        //Auth token
        DiskClient.token = token;

        //Add authenticator
        authInterceptor = new AuthInterceptor();
        authInterceptor.setToken(token);

        //Create http client
        OkHttpClient httpClient = createHttpClient(context.getCacheDir(), "response-cache");

        //Create gson object
        Gson gson = new GsonBuilder()
                .setDateFormat("dd.MM.yyyy")
                .create();

        //Build retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build();

        //Create api
        api = retrofit.create(DiskApi.class);
    }

    /**
     * Create picasso with {@link OkHttpClient} http client
     * to provide {@link Cache} image caching.
     */
    public void createPicasso(Context context) {

        //Create http client for picasso downloader
        OkHttpClient picassoClient = createHttpClient(context.getCacheDir(), "picasso-cache");

        //Build picasso instance
        Picasso picasso = new Picasso.Builder(context)
                .indicatorsEnabled(BuildConfig.DEBUG)
                .loggingEnabled(BuildConfig.DEBUG)
                .downloader(new OkHttp3Downloader(picassoClient))
                .listener((pic, uri, exception) -> exception.printStackTrace())
                .build();

        Picasso.setSingletonInstance(picasso);
    }

    /** Create http client {@link OkHttpClient}. */
    private OkHttpClient createHttpClient(File cacheDir, String cacheName) {

        NetworkConnectionInterceptor networkConnectionInterceptor = new NetworkConnectionInterceptor() {
            @Override
            public boolean isInternetAvailable() {
                return DiskClient.this.isInternetAvailable();
            }
        };

        //Build http client for api
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .cache(getCache(cacheDir, cacheName))
                .addInterceptor(authInterceptor)
                .addInterceptor(networkConnectionInterceptor);

        //Add logging in debug
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        return builder.build();
    }

    /** Get current Internet state. */
    private boolean isInternetAvailable() {
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }

    /** Provide cache {@link Cache}. */
    private Cache getCache(File cacheDir, String name) {
        return new Cache(new File(cacheDir, name), DISK_CACHE_SIZE);
    }
}
