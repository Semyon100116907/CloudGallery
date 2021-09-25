package com.semisonfire.cloudgallery.adapter.factory

import android.view.LayoutInflater
import android.view.ViewGroup
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.holder.ItemViewHolder

interface AdapterFactory {
    fun create(parent: ViewGroup, viewType: Int): ItemViewHolder<out Item>
    fun getItemViewType(items: List<Item>, position: Int): Int
}

internal class AdapterFactoryImpl(
    adapterProviders: Set<ItemProvider>
) : AdapterFactory {

    private val providers = adapterProviders.toList()

    override fun create(parent: ViewGroup, viewType: Int): ItemViewHolder<out Item> {

        val itemProvider = providers[viewType]
        val view = itemProvider.createView(
            inflater = LayoutInflater.from(parent.context),
            parent = parent,
            viewType = viewType
        )

        return itemProvider.provideViewHolder(view, viewType)
    }

    override fun getItemViewType(items: List<Item>, position: Int): Int {

        val providersCount = providers.size
        for (i in 0 until providersCount) {
            val provider = providers[i]
            if (provider.checkItemViewType(items[position])) {
                return i
            }
        }

        return -1
    }
}