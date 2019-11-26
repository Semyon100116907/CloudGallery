package com.semisonfire.cloudgallery.ui.main.di

import android.support.v7.app.AppCompatActivity
import com.semisonfire.cloudgallery.core.ui.navigation.Navigator
import com.semisonfire.cloudgallery.core.ui.navigation.NavigatorImpl
import com.semisonfire.cloudgallery.core.di.ActivityScope
import com.semisonfire.cloudgallery.core.di.module.ActivityModule
import com.semisonfire.cloudgallery.core.data.remote.auth.AuthRepository
import com.semisonfire.cloudgallery.ui.main.MainActivity
import com.semisonfire.cloudgallery.ui.main.MainPresenter
import com.semisonfire.cloudgallery.ui.main.MainPresenterImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [MainModule.Declarations::class])
class MainModule {

  @Module(includes = [ActivityModule::class])
  interface Declarations {
    @Binds
    @ActivityScope
    fun activity(mainActivity: MainActivity): AppCompatActivity
  }

  @Provides
  @ActivityScope
  fun provideMainPresenter(authRepository: AuthRepository): MainPresenter {
    return MainPresenterImpl(authRepository)
  }

  @Provides
  @ActivityScope
  fun provideNavigator(activity: AppCompatActivity): Navigator {
    return NavigatorImpl(activity.supportFragmentManager)
  }
}