package com.semisonfire.cloudgallery.ui.trash.di

import com.semisonfire.cloudgallery.data.remote.RemoteRepository
import com.semisonfire.cloudgallery.core.di.FragmentScope
import com.semisonfire.cloudgallery.ui.trash.TrashContract
import com.semisonfire.cloudgallery.ui.trash.TrashPresenter
import dagger.Module
import dagger.Provides

@Module
class TrashModule {

  @Provides
  @FragmentScope
  fun provideTrashPresenter(
    remoteRepository: RemoteRepository
  ): TrashContract.Presenter {
    return TrashPresenter(remoteRepository)
  }
}