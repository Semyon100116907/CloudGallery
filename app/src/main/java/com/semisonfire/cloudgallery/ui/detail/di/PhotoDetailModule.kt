package com.semisonfire.cloudgallery.ui.detail.di

import com.semisonfire.cloudgallery.di.annotation.ActivityScope
import com.semisonfire.cloudgallery.ui.detail.PhotoDetailPresenter
import com.semisonfire.cloudgallery.ui.detail.PhotoDetailPresenterImpl
import dagger.Binds
import dagger.Module

@Module
internal abstract class PhotoDetailModule {

    @Binds
    @ActivityScope
    abstract fun bindsPhotoDetailPresenter(impl: PhotoDetailPresenterImpl): PhotoDetailPresenter
}