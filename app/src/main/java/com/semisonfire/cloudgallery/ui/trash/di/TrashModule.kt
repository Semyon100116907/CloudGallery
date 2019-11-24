package com.semisonfire.cloudgallery.ui.trash.di

import com.semisonfire.cloudgallery.data.remote.RemoteDataSource
import com.semisonfire.cloudgallery.di.FragmentScope
import com.semisonfire.cloudgallery.ui.trash.TrashContract
import com.semisonfire.cloudgallery.ui.trash.TrashPresenter
import dagger.Module
import dagger.Provides

@Module
class TrashModule {

  @Provides
  @FragmentScope
  fun provideTrashPresenter(
    remoteDataSource: RemoteDataSource
  ): TrashContract.Presenter {
    return TrashPresenter(remoteDataSource)
  }
}