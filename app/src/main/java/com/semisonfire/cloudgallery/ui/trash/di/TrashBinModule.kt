package com.semisonfire.cloudgallery.ui.trash.di

import com.semisonfire.cloudgallery.adapter.factory.AdapterFactory
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactoryProvider
import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.ui.trash.TrashBinPresenter
import com.semisonfire.cloudgallery.ui.trash.TrashBinPresenterImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class TrashBinModule {

    companion object {

        @Provides
        @FragmentScope
        fun provideAdapterFactory(adapterFactoryProvider: AdapterFactoryProvider): AdapterFactory {
            return adapterFactoryProvider.create(emptySet())
        }
    }

    @Binds
    @FragmentScope
    abstract fun bindsTrashPresenter(impl: TrashBinPresenterImpl): TrashBinPresenter
}