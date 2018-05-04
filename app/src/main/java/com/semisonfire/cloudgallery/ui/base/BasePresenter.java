package com.semisonfire.cloudgallery.ui.base;

import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class BasePresenter<T extends MvpView> implements MvpPresenter<T> {

    private T mMvpView;
    private CompositeDisposable mCompositeDisposable;
    private DiskPreferences mDiskPreferences;

    public BasePresenter() {
        this.mCompositeDisposable = new CompositeDisposable();
    }

    public BasePresenter(DiskPreferences preferences) {
        this();
        mDiskPreferences = preferences;
    }

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    @Override
    public void getCachedToken() {
        getCompositeDisposable().add(
                Single.fromCallable(() -> mDiskPreferences.getPrefToken())
                        .onErrorReturnItem("")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(token -> getMvpView().onTokenLoaded(token)));
    }

    @Override
    public void setCachedToken(String cachedToken) {
        getCompositeDisposable().add(
                Single.just(cachedToken)
                        .map(token -> {
                            mDiskPreferences.setPrefToken(token);
                            return token;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe());
    }

    @Override
    public void attachView(T view) {
        mMvpView = view;
    }

    @Override
    public void detachView() {
        mMvpView = null;
    }

    public boolean isViewAttached() {
        return mMvpView != null;
    }

    public T getMvpView() {
        return mMvpView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }

    public void dispose() {
        detachView();
        mCompositeDisposable.dispose();
    }

    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call MvpPresenter.attachView(MvpView) before" +
                    " requesting data to the MvpPresenter");
        }
    }
}
