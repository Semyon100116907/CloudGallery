package com.semisonfire.cloudgallery.di.builder

import com.semisonfire.cloudgallery.di.ActivityScope
import com.semisonfire.cloudgallery.ui.main.MainActivity
import com.semisonfire.cloudgallery.ui.main.di.MainModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityProvider {

  @ActivityScope
  @ContributesAndroidInjector(modules = [MainModule::class])
  internal abstract fun contributeMainActivity(): MainActivity
}

