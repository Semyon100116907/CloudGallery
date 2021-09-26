package com.semisonfire.cloudgallery.adapter.di

import com.semisonfire.cloudgallery.adapter.di.annotation.AdapterScope
import com.semisonfire.cloudgallery.adapter.di.annotation.GroupItemProvider
import com.semisonfire.cloudgallery.adapter.factory.ItemProvider
import com.semisonfire.cloudgallery.adapter.progress.ProgressItemProvider
import com.semisonfire.cloudgallery.common.photo.PhotoItemProvider
import com.semisonfire.cloudgallery.common.scroll.HorizontalScrollItemProvider
import com.semisonfire.cloudgallery.common.title.TitleItemProvider
import com.semisonfire.cloudgallery.ui.disk.adapter.upload.UploadItemProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
internal abstract class AdapterProvidersModule {

    @Binds
    @IntoSet
    @AdapterScope
    abstract fun bindProgressItemProvider(impl: ProgressItemProvider): ItemProvider

    @Binds
    @IntoSet
    @AdapterScope
    abstract fun bindTitleItemProvider(impl: TitleItemProvider): ItemProvider

    @Binds
    @IntoSet
    @AdapterScope
    abstract fun bindPhotoItemProvider(impl: PhotoItemProvider): ItemProvider

    @Binds
    @IntoSet
    @AdapterScope
    abstract fun bindUploadItemProvider(impl: UploadItemProvider): ItemProvider

    @Binds
    @IntoSet
    @AdapterScope
    @GroupItemProvider
    abstract fun bindHorizontalItemProvider(impl: HorizontalScrollItemProvider): ItemProvider
}