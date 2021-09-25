package com.semisonfire.cloudgallery.ui.disk.adapter

import com.semisonfire.cloudgallery.adapter.LoadMoreAdapter
import com.semisonfire.cloudgallery.adapter.factory.AdapterFactory
import com.semisonfire.cloudgallery.adapter.holder.Item
import javax.inject.Inject

class DiskAdapter @Inject constructor(adapterFactory: AdapterFactory) :
    LoadMoreAdapter<Item>(adapterFactory)