package com.semisonfire.cloudgallery.ui.trash.di

import com.semisonfire.cloudgallery.di.AppComponent
import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.ui.trash.TrashFragment
import dagger.Component

@Component(
    modules = [
        TrashModule::class
    ],
    dependencies = [
        AppComponent::class
    ]
)
@FragmentScope
interface TrashBinComponent {

    fun inject(screen: TrashFragment)

    @Component.Factory
    interface Factory {

        fun create(
            appComponent: AppComponent
        ): TrashBinComponent
    }
}