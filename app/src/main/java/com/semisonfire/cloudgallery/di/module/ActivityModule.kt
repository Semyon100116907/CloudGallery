package com.semisonfire.cloudgallery.di.module

import com.semisonfire.cloudgallery.di.builder.FragmentProvider
import dagger.Module

@Module(includes = [FragmentProvider::class])
abstract class ActivityModule


