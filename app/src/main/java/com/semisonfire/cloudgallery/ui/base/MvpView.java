package com.semisonfire.cloudgallery.ui.base;

public interface MvpView {

    void onSuccess(String message);

    void onError(Throwable throwable);

    void onTokenLoaded(String token);
}
