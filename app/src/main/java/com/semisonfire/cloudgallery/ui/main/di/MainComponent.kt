package com.semisonfire.cloudgallery.ui.main.di

import androidx.appcompat.app.AppCompatActivity
import com.semisonfire.cloudgallery.di.AppComponent
import com.semisonfire.cloudgallery.di.annotation.ActivityScope
import com.semisonfire.cloudgallery.di.api.NavigationComponentApi
import com.semisonfire.cloudgallery.ui.main.MainActivity
import dagger.BindsInstance
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
interface MainComponent : NavigationComponentApi {

    fun inject(screen: MainActivity)

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance activity: AppCompatActivity,
            appComponent: AppComponent
        ): MainComponent
    }
}