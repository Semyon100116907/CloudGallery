package com.semisonfire.cloudgallery.ui.settings.di

import com.semisonfire.cloudgallery.core.di.FragmentScope
import com.semisonfire.cloudgallery.ui.settings.SettingsPresenter
import com.semisonfire.cloudgallery.ui.settings.SettingsPresenterImpl
import dagger.Module
import dagger.Provides

@Module
class SettingsModule {

    @Provides
    @FragmentScope
    fun provideSettingsPresenter(): SettingsPresenter {
        return SettingsPresenterImpl()
    }
}