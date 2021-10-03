package com.semisonfire.cloudgallery.ui.detail.di

import com.semisonfire.cloudgallery.di.AppComponent
import com.semisonfire.cloudgallery.di.annotation.ActivityScope
import com.semisonfire.cloudgallery.ui.detail.PhotoDetailActivity
import dagger.Component

@Component(
    modules = [
        PhotoDetailModule::class
    ],
    dependencies = [
        AppComponent::class
    ]
)
@ActivityScope
interface PhotoDetailComponent {

    fun inject(screen: PhotoDetailActivity)

    @Component.Factory
    interface Factory {
        fun create(
            appComponent: AppComponent
        ): PhotoDetailComponent
    }
}