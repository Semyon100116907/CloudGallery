package com.semisonfire.cloudgallery.ui.disk.di

import com.semisonfire.cloudgallery.adapter.factory.AdapterFactory
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactoryProvider
import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.ui.disk.DiskPresenter
import com.semisonfire.cloudgallery.ui.disk.DiskPresenterImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class DiskModule {

    companion object {

        @Provides
        @FragmentScope
        fun provideAdapterFactory(provider: AdapterFactoryProvider): AdapterFactory {
            return provider.create(emptySet())
        }
    }

    @Binds
    @FragmentScope
    abstract fun bindsDiskPresenter(impl: DiskPresenterImpl): DiskPresenter
}