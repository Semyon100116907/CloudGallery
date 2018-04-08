package com.semisonfire.cloudgallery.ui.base;

public interface MvpPresenter<T extends MvpView> {

    void attachView(T view);

    void detachView();

    void getCachedToken();

    void setCachedToken(String token);
}
