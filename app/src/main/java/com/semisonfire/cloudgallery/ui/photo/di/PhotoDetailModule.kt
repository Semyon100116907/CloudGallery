package com.semisonfire.cloudgallery.ui.photo.di

import androidx.appcompat.app.AppCompatActivity
import com.semisonfire.cloudgallery.core.di.ActivityScope
import com.semisonfire.cloudgallery.core.di.module.ActivityModule
import com.semisonfire.cloudgallery.core.ui.navigation.Navigator
import com.semisonfire.cloudgallery.core.ui.navigation.NavigatorImpl
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenter
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailPresenterImpl
import com.semisonfire.cloudgallery.ui.trash.data.TrashRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module(includes = [PhotoDetailModule.Declarations::class])
class PhotoDetailModule {

    @Module(includes = [ActivityModule::class])
    interface Declarations {
        @Binds
        @ActivityScope
        @Named("DETAIL")
        fun activity(mainActivity: PhotoDetailActivity): AppCompatActivity
    }

    @Provides
    @ActivityScope
    fun providesPhotoDetailPresenter(
        diskRepository: DiskRepository,
        trashRepository: TrashRepository
    ): PhotoDetailPresenter {
        return PhotoDetailPresenterImpl(diskRepository, trashRepository)
    }

    @Provides
    @ActivityScope
    fun provideNavigator(@Named("DETAIL") activity: AppCompatActivity): Navigator {
        return NavigatorImpl(activity.supportFragmentManager)
    }
}