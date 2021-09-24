package com.semisonfire.cloudgallery.di.api

import com.semisonfire.cloudgallery.core.ui.navigation.router.Router

interface NavigationComponentApi {
    fun router(): Router
}