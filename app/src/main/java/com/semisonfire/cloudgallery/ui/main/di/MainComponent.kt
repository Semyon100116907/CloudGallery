package com.semisonfire.cloudgallery.ui.main.di

import com.semisonfire.cloudgallery.di.AppComponent
import com.semisonfire.cloudgallery.di.annotation.ActivityScope
import com.semisonfire.cloudgallery.ui.main.MainActivity
import dagger.Component

@Component(
    modules = [
        MainModule::class
    ],
    dependencies = [
        AppComponent::class
    ]
)
@ActivityScope
interface MainComponent {

    fun inject(screen: MainActivity)

    @Component.Factory
    interface Factory {

        fun create(
            appComponent: AppComponent
        ): MainComponent
    }
}