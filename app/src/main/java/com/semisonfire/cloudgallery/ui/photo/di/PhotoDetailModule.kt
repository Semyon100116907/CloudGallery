package com.semisonfire.cloudgallery.ui.photo.di

import com.semisonfire.cloudgallery.core.di.ActivityScope
import com.semisonfire.cloudgallery.data.remote.RemoteRepository
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenter
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenterImpl
import dagger.Module
import dagger.Provides

@Module
class PhotoDetailModule {

  @Provides
  @ActivityScope
  fun providesPhotoDetailPresenter(remoteRepository: RemoteRepository): PhotoDetailPresenter {
    return PhotoDetailPresenterImpl(remoteRepository)
  }
}