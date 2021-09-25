package com.semisonfire.cloudgallery.ui.main.di

import com.semisonfire.cloudgallery.di.annotation.ActivityScope
import com.semisonfire.cloudgallery.ui.main.MainPresenter
import com.semisonfire.cloudgallery.ui.main.MainPresenterImpl
import dagger.Binds
import dagger.Module

@Module
abstract class MainModule {

    @Binds
    @ActivityScope
    abstract fun bindsMainPresenter(impl: MainPresenterImpl): MainPresenter
}