package com.semisonfire.cloudgallery.ui.trash.di

import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.ui.trash.TrashPresenter
import com.semisonfire.cloudgallery.ui.trash.TrashPresenterImpl
import com.semisonfire.cloudgallery.ui.trash.data.TrashRepository
import dagger.Module
import dagger.Provides

@Module
class TrashModule {

    @Provides
    @FragmentScope
    fun provideTrashPresenter(
        trashRepository: TrashRepository
    ): TrashPresenter {
        return TrashPresenterImpl(trashRepository)
    }
}