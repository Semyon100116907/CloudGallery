package com.semisonfire.cloudgallery.di.builder

import com.semisonfire.cloudgallery.di.ActivityScope
import com.semisonfire.cloudgallery.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector
import javax.inject.Scope

@Module
abstract class ActivityProvider {

  @ActivityScope
  @ContributesAndroidInjector(modules = [])
  internal abstract fun contributeMainActivity(): MainActivity
}

