package com.semisonfire.cloudgallery.navigation.di.module

import com.semisonfire.cloudgallery.navigation.di.annotation.NavigationScope
import com.semisonfire.cloudgallery.navigation.navigator.Navigator
import com.semisonfire.cloudgallery.navigation.router.Router
import com.semisonfire.cloudgallery.navigation.router.RouterImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [NavigatorsModule::class])
internal abstract class NavigationModule {

    companion object {

        @Provides
        @NavigationScope
        fun provideNavigatorMap(
            navigators: Set<@JvmSuppressWildcards Navigator>
        ): Map<String, Navigator> {
            val navigationMap = HashMap<String, Navigator>(navigators.size)
            navigators.forEach { navigationMap[it.key] = it }

            return navigationMap
        }
    }

    @Binds
    @NavigationScope
    abstract fun bindRouter(router: RouterImpl): Router
}