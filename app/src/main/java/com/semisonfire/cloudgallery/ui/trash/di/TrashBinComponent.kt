package com.semisonfire.cloudgallery.ui.trash.di

import com.semisonfire.cloudgallery.di.AppComponent
import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.ui.trash.TrashBinFragment
import dagger.Component

@Component(
    modules = [
        TrashBinModule::class
    ],
    dependencies = [
        AppComponent::class
    ]
)
@FragmentScope
interface TrashBinComponent {

    fun inject(screen: TrashBinFragment)

    @Component.Factory
    interface Factory {

        fun create(
            appComponent: AppComponent
        ): TrashBinComponent
    }
}