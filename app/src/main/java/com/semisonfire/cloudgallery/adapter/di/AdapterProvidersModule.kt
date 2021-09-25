package com.semisonfire.cloudgallery.adapter.di

import com.semisonfire.cloudgallery.adapter.di.annotation.AdapterScope
import com.semisonfire.cloudgallery.adapter.factory.ItemProvider
import com.semisonfire.cloudgallery.adapter.progress.ProgressItemProvider
import dagger.Binds
import dagger.Module

@Module
internal abstract class AdapterProvidersModule {

    @Binds
    @AdapterScope
    abstract fun bindProgressItemProvider(impl: ProgressItemProvider): ItemProvider
}