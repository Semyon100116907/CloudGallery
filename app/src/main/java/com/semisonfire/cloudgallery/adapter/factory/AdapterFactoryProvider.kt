package com.semisonfire.cloudgallery.adapter.factory

import com.semisonfire.cloudgallery.adapter.di.annotation.GroupItemProvider
import javax.inject.Inject

interface AdapterFactoryProvider {

    fun create(
        providers: Set<ItemProvider>
    ): AdapterFactory
}

internal class AdapterFactoryProviderImpl @Inject constructor(
    @GroupItemProvider
    groupItemProvider: Set<@JvmSuppressWildcards ItemProvider>,
    commonProviders: Set<@JvmSuppressWildcards ItemProvider>
) : AdapterFactoryProvider {

    private val providers = groupItemProvider + commonProviders

    override fun create(providers: Set<ItemProvider>): AdapterFactory {
        return AdapterFactoryImpl(this.providers + providers)
    }
}