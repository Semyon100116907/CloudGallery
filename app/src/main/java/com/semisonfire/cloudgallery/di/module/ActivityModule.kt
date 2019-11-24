package com.semisonfire.cloudgallery.di.module

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.semisonfire.cloudgallery.di.ActivityContext
import com.semisonfire.cloudgallery.di.ActivityScope
import com.semisonfire.cloudgallery.di.builder.FragmentProvider
import dagger.Binds
import dagger.Module

@Module(includes = [FragmentProvider::class])
abstract class ActivityModule {
  @Binds
  @ActivityScope
  @ActivityContext
  internal abstract fun activityContext(activity: AppCompatActivity): Context
}


