package com.semisonfire.cloudgallery.ui.main;

import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.ui.base.BasePresenter;

public class MainPresenter<V extends MainContract.View> extends BasePresenter<V>
        implements MainContract.Presenter<V>{

    public MainPresenter(DiskPreferences preferences) {
        super(preferences);
    }
}
