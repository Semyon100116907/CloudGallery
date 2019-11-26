package com.semisonfire.cloudgallery.ui.photo.di

import com.semisonfire.cloudgallery.core.di.ActivityScope
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenter
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenterImpl
import com.semisonfire.cloudgallery.ui.trash.data.TrashRepository
import dagger.Module
import dagger.Provides

@Module
class PhotoDetailModule {

  @Provides
  @ActivityScope
  fun providesPhotoDetailPresenter(
    diskRepository: DiskRepository,
    trashRepository: TrashRepository
  ): PhotoDetailPresenter {

    return PhotoDetailPresenterImpl(diskRepository, trashRepository)
  }
}