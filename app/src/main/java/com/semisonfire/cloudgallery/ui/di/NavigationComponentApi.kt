package com.semisonfire.cloudgallery.ui.di

import com.semisonfire.cloudgallery.core.ui.navigation.router.Router

interface NavigationComponentApi {
    fun router(): Router
}