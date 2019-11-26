package com.semisonfire.cloudgallery.core.di.module

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.semisonfire.cloudgallery.core.di.ActivityContext
import com.semisonfire.cloudgallery.core.di.ActivityScope
import com.semisonfire.cloudgallery.core.di.builder.FragmentProvider
import dagger.Binds
import dagger.Module

@Module(includes = [FragmentProvider::class])
abstract class ActivityModule {
  @Binds
  @ActivityScope
  @ActivityContext
  internal abstract fun activityContext(activity: AppCompatActivity): Context
}


