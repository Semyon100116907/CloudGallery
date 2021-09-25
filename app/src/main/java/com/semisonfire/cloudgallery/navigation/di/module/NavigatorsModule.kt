package com.semisonfire.cloudgallery.navigation.di.module

import com.semisonfire.cloudgallery.navigation.navigator.Navigator
import com.semisonfire.cloudgallery.ui.disk.DiskNavigator
import com.semisonfire.cloudgallery.ui.settings.SettingsNavigator
import com.semisonfire.cloudgallery.ui.trash.TrashBinNavigator
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
internal abstract class NavigatorsModule {

    @Binds
    @IntoSet
    abstract fun bindsDiskNavigator(impl: DiskNavigator): Navigator

    @Binds
    @IntoSet
    abstract fun bindsTrashBinNavigator(impl: TrashBinNavigator): Navigator

    @Binds
    @IntoSet
    abstract fun bindsSettingsNavigator(impl: SettingsNavigator): Navigator
}