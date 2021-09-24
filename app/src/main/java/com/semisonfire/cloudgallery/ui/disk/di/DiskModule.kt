package com.semisonfire.cloudgallery.ui.disk.di

import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.ui.disk.DiskPresenter
import com.semisonfire.cloudgallery.ui.disk.DiskPresenterImpl
import dagger.Binds
import dagger.Module

@Module
abstract class DiskModule {

    @Binds
    @FragmentScope
    abstract fun bindsDiskPresenter(impl: DiskPresenterImpl): DiskPresenter
}