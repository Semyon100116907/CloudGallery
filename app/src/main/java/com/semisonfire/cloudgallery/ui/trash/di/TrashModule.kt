package com.semisonfire.cloudgallery.ui.trash.di

import com.semisonfire.cloudgallery.adapter.factory.AdapterFactory
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactoryProvider
import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.ui.trash.TrashPresenter
import com.semisonfire.cloudgallery.ui.trash.TrashPresenterImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class TrashModule {

    companion object {

        @Provides
        @FragmentScope
        fun provideAdapterFactory(adapterFactoryProvider: AdapterFactoryProvider): AdapterFactory {
            return adapterFactoryProvider.create(emptySet())
        }
    }

    @Binds
    @FragmentScope
    abstract fun bindsTrashPresenter(impl: TrashPresenterImpl): TrashPresenter
}