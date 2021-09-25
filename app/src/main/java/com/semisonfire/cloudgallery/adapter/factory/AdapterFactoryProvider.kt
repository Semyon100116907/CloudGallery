package com.semisonfire.cloudgallery.adapter.factory

import javax.inject.Inject

interface AdapterFactoryProvider {

    fun create(
        providers: Set<ItemProvider>
    ): AdapterFactory
}

internal class AdapterFactoryProviderImpl @Inject constructor(
    private val commonProviders: Set<@JvmSuppressWildcards ItemProvider>
) : AdapterFactoryProvider {

    override fun create(providers: Set<ItemProvider>): AdapterFactory {
        return AdapterFactoryImpl(commonProviders + providers)
    }
}