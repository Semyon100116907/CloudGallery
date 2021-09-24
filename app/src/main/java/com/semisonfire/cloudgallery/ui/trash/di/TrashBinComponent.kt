package com.semisonfire.cloudgallery.ui.trash.di

import com.semisonfire.cloudgallery.di.AppComponent
import com.semisonfire.cloudgallery.di.annotation.FragmentScope
import com.semisonfire.cloudgallery.di.api.NavigationComponentApi
import com.semisonfire.cloudgallery.ui.trash.TrashFragment
import dagger.Component

@Component(
    modules = [
        TrashModule::class
    ],
    dependencies = [
        AppComponent::class,
        NavigationComponentApi::class
    ]
)
@FragmentScope
interface TrashBinComponent {

    fun inject(screen: TrashFragment)

    @Component.Factory
    interface Factory {

        fun create(
            appComponent: AppComponent,
            navigationComponentApi: NavigationComponentApi
        ): TrashBinComponent
    }
}