package com.semisonfire.cloudgallery.ui.disk.di

import com.semisonfire.cloudgallery.core.di.FragmentScope
import com.semisonfire.cloudgallery.ui.disk.DiskPresenter
import com.semisonfire.cloudgallery.ui.disk.DiskPresenterImpl
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.disk.data.UploadRepository
import dagger.Module
import dagger.Provides

@Module
class DiskModule {

    @Provides
    @FragmentScope
    fun provideDiskPresenter(
        diskRepository: DiskRepository,
        uploadRepository: UploadRepository
    ): DiskPresenter {
        return DiskPresenterImpl(diskRepository, uploadRepository)
    }
}