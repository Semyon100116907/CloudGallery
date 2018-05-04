package com.semisonfire.cloudgallery.ui.base;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public abstract class BaseActivity extends AppCompatActivity implements MvpView {

    private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    public void onTokenLoaded(String token) {

    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable != null) {
            Log.e(TAG, "onError: " + throwable.getMessage(), throwable);
        }
    }

    public abstract void bind();
}
