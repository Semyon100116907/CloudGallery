package com.semisonfire.cloudgallery.ui.main;

import com.semisonfire.cloudgallery.ui.base.MvpPresenter;
import com.semisonfire.cloudgallery.ui.base.MvpView;

public interface MainContract {

    interface View extends MvpView {

    }

    interface Presenter<V extends View> extends MvpPresenter<V> {

    }
}
