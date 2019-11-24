package com.semisonfire.cloudgallery.di.module

import com.semisonfire.cloudgallery.di.module.data.local.RoomModule
import com.semisonfire.cloudgallery.di.module.data.local.SharedPreferencesModule
import com.semisonfire.cloudgallery.di.module.data.remote.RetrofitModule
import dagger.Module

@Module(
  includes = [
    ContextModule::class,
    RoomModule::class,
    SharedPreferencesModule::class,
    RetrofitModule::class
  ]
)
class AppModule