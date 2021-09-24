package com.semisonfire.cloudgallery.ui.main.di

import androidx.appcompat.app.AppCompatActivity
import com.semisonfire.cloudgallery.core.ui.navigation.Navigator
import com.semisonfire.cloudgallery.core.ui.navigation.NavigatorImpl
import com.semisonfire.cloudgallery.di.annotation.ActivityScope
import com.semisonfire.cloudgallery.ui.main.MainPresenter
import com.semisonfire.cloudgallery.ui.main.MainPresenterImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class MainModule {

    companion object {

        @Provides
        @ActivityScope
        fun provideNavigator(activity: AppCompatActivity): Navigator {
            return NavigatorImpl(activity.supportFragmentManager)
        }
    }

    @Binds
    @ActivityScope
    abstract fun bindsMainPresenter(impl: MainPresenterImpl): MainPresenter
}