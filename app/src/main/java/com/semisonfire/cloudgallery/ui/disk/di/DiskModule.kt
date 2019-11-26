package com.semisonfire.cloudgallery.ui.disk.di

import com.semisonfire.cloudgallery.core.di.FragmentScope
import com.semisonfire.cloudgallery.data.local.LocalRepository
import com.semisonfire.cloudgallery.ui.disk.DiskPresenter
import com.semisonfire.cloudgallery.ui.disk.DiskPresenterImpl
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import dagger.Module
import dagger.Provides

@Module
class DiskModule {

  @Provides
  @FragmentScope
  fun provideDiskPresenter(
    diskRepository: DiskRepository,
    localRepository: LocalRepository
  ): DiskPresenter {
    return DiskPresenterImpl(diskRepository, localRepository)
  }
}