package com.semisonfire.cloudgallery.ui.base;

public interface MvpView {

    void onError(Throwable throwable);

    void onTokenLoaded(String token);
}
