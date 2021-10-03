package com.semisonfire.cloudgallery.ui.trash.adapter

import com.semisonfire.cloudgallery.adapter.LoadMoreAdapter
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactory
import com.semisonfire.cloudgallery.adapter.holder.Item
import javax.inject.Inject

class TrashBinAdapter @Inject constructor(factory: AdapterFactory) : LoadMoreAdapter<Item>(factory)