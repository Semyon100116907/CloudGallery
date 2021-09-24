package com.semisonfire.cloudgallery.core.di.module

import com.semisonfire.cloudgallery.core.di.module.data.local.RoomModule
import com.semisonfire.cloudgallery.core.di.module.data.local.SharedPreferencesModule
import com.semisonfire.cloudgallery.core.di.module.data.remote.RetrofitModule
import dagger.Module

@Module(
    includes = [
        RoomModule::class,
        SharedPreferencesModule::class,
        RetrofitModule::class
    ]
)
class AppModule