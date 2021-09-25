package com.semisonfire.cloudgallery.ui.photo.di

import com.semisonfire.cloudgallery.di.annotation.ActivityScope
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenter
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenterImpl
import dagger.Binds
import dagger.Module

@Module
internal abstract class PhotoDetailModule {

    @Binds
    @ActivityScope
    abstract fun bindsPhotoDetailPresenter(impl: PhotoDetailPresenterImpl): PhotoDetailPresenter
}