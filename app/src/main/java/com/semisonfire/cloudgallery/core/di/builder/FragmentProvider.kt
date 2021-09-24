package com.semisonfire.cloudgallery.core.di.builder

import com.semisonfire.cloudgallery.core.di.FragmentScope
import com.semisonfire.cloudgallery.ui.disk.DiskFragment
import com.semisonfire.cloudgallery.ui.disk.di.DiskModule
import com.semisonfire.cloudgallery.ui.settings.SettingsFragment
import com.semisonfire.cloudgallery.ui.settings.di.SettingsModule
import com.semisonfire.cloudgallery.ui.trash.TrashFragment
import com.semisonfire.cloudgallery.ui.trash.di.TrashModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentProvider {

    @FragmentScope
    @ContributesAndroidInjector(modules = [DiskModule::class])
    internal abstract fun contributeDiskFragment(): DiskFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [TrashModule::class])
    internal abstract fun contributeTrashFragment(): TrashFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [SettingsModule::class])
    internal abstract fun contributeSettingsFragment(): SettingsFragment
}