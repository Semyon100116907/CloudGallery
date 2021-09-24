package com.semisonfire.cloudgallery.ui.trash.di

import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.ui.trash.TrashPresenter
import com.semisonfire.cloudgallery.ui.trash.TrashPresenterImpl
import dagger.Binds
import dagger.Module

@Module
internal abstract class TrashModule {

    @Binds
    @FragmentScope
    abstract fun bindsTrashPresenter(impl: TrashPresenterImpl): TrashPresenter
}