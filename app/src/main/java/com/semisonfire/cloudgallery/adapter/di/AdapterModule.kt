package com.semisonfire.cloudgallery.adapter.di

import com.semisonfire.cloudgallery.adapter.di.annotation.AdapterScope
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactoryProvider
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactoryProviderImpl
import dagger.Binds
import dagger.Module

@Module(
    includes = [AdapterProvidersModule::class]
)
internal abstract class AdapterModule {

    @Binds
    @AdapterScope
    abstract fun bindAdapterFactoryProvider(impl: AdapterFactoryProviderImpl): AdapterFactoryProvider
}