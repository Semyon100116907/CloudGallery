package com.semisonfire.cloudgallery.core.di.builder

import com.semisonfire.cloudgallery.core.di.ActivityScope
import com.semisonfire.cloudgallery.ui.main.MainActivity
import com.semisonfire.cloudgallery.ui.main.di.MainModule
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity
import com.semisonfire.cloudgallery.ui.photo.di.PhotoDetailModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityProvider {

  @ActivityScope
  @ContributesAndroidInjector(modules = [MainModule::class])
  internal abstract fun contributeMainActivity(): MainActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [PhotoDetailModule::class])
  internal abstract fun contributePhotoDetailActivity(): PhotoDetailActivity
}

