package com.semisonfire.cloudgallery.di.builder

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

  @ContributesAndroidInjector(modules = [DiskModule::class])
  internal abstract fun contributeDiskFragment(): DiskFragment

  @ContributesAndroidInjector(modules = [TrashModule::class])
  internal abstract fun contributeTrashFragment(): TrashFragment

  @ContributesAndroidInjector(modules = [SettingsModule::class])
  internal abstract fun contributeSettingsFragment(): SettingsFragment
}