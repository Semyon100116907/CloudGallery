package com.semisonfire.cloudgallery.ui.photo.di

import androidx.appcompat.app.AppCompatActivity
import com.semisonfire.cloudgallery.di.AppComponent
import com.semisonfire.cloudgallery.di.annotation.ActivityScope
import com.semisonfire.cloudgallery.di.api.NavigationComponentApi
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity
import dagger.BindsInstance
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
interface PhotoDetailComponent : NavigationComponentApi {

    fun inject(screen: PhotoDetailActivity)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance activity: AppCompatActivity,
            appComponent: AppComponent
        ): PhotoDetailComponent
    }
}