package com.semisonfire.cloudgallery.ui.photo.di

import androidx.appcompat.app.AppCompatActivity
import com.semisonfire.cloudgallery.core.ui.navigation.Navigator
import com.semisonfire.cloudgallery.core.ui.navigation.NavigatorImpl
import com.semisonfire.cloudgallery.di.annotation.ActivityScope
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenter
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenterImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class PhotoDetailModule {

    companion object {
        @Provides
        @ActivityScope
        fun provideNavigator(activity: AppCompatActivity): Navigator {
            return NavigatorImpl(activity.supportFragmentManager)
        }
    }

    @Binds
    @ActivityScope
    abstract fun bindsPhotoDetailPresenter(impl: PhotoDetailPresenterImpl): PhotoDetailPresenter
}