package com.semisonfire.cloudgallery.ui.disk.di

import com.semisonfire.cloudgallery.core.di.FragmentScope
import com.semisonfire.cloudgallery.data.local.LocalRepository
import com.semisonfire.cloudgallery.data.remote.RemoteRepository
import com.semisonfire.cloudgallery.ui.disk.DiskPresenter
import com.semisonfire.cloudgallery.ui.disk.DiskPresenterImpl
import dagger.Module
import dagger.Provides

@Module
class DiskModule {

  @Provides
  @FragmentScope
  fun provideDiskPresenter(
    remoteRepository: RemoteRepository,
    localRepository: LocalRepository
  ): DiskPresenter {
    return DiskPresenterImpl(remoteRepository, localRepository)
  }
}