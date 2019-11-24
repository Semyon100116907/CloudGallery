package com.semisonfire.cloudgallery.di.module

import android.content.Context
import com.semisonfire.cloudgallery.di.AppContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(private val context: Context) {

  @Provides
  @Singleton
  @AppContext
  internal fun context(): Context {
    return context.applicationContext
  }
}