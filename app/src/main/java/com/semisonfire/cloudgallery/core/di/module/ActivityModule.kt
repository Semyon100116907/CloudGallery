package com.semisonfire.cloudgallery.core.di.module

import com.semisonfire.cloudgallery.core.di.builder.FragmentProvider
import dagger.Module

@Module(includes = [FragmentProvider::class])
abstract class ActivityModule


