package com.kennyc.dashweather.di

import android.content.Context
import com.kennyc.dashweather.api_owm.di.OWMModule
import com.kennyc.dashweather.di.modules.RepositoryModule
import com.kennyc.dashweather.di.modules.SharedPreferencesModule
import com.kennyc.dashweather.services.DarkSkyDashExtension
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [OWMModule::class, SharedPreferencesModule::class, RepositoryModule::class])
interface AppComponent {

    fun inject(service: DarkSkyDashExtension)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun appContext(context: Context): Builder

        @BindsInstance
        fun isDebug(@Named("isDebug") isDebug: Boolean): Builder

        fun build(): AppComponent
    }
}