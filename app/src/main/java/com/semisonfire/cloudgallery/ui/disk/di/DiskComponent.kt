package com.semisonfire.cloudgallery.ui.disk.di

import com.semisonfire.cloudgallery.di.AppComponent
import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.ui.disk.DiskFragment
import dagger.Component

@Component(
    modules = [
        DiskModule::class
    ],
    dependencies = [
        AppComponent::class
    ]
)
@FragmentScope
interface DiskComponent {

    fun inject(screen: DiskFragment)

    @Component.Factory
    interface Factory {

        fun create(
            appComponent: AppComponent
        ): DiskComponent
    }
}