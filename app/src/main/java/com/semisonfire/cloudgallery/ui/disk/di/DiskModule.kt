package com.semisonfire.cloudgallery.ui.disk.di

import com.semisonfire.cloudgallery.data.local.LocalDataSource
import com.semisonfire.cloudgallery.data.remote.RemoteDataSource
import com.semisonfire.cloudgallery.di.FragmentScope
import com.semisonfire.cloudgallery.ui.disk.DiskContract
import com.semisonfire.cloudgallery.ui.disk.DiskPresenter
import dagger.Module
import dagger.Provides

@Module
class DiskModule {

  @Provides
  @FragmentScope
  fun provideDiskPresenter(
    remoteDataSource: RemoteDataSource,
    localDataSource: LocalDataSource
  ): DiskContract.Presenter {
    return DiskPresenter(remoteDataSource, localDataSource)
  }
}