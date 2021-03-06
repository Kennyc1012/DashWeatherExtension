package com.kennyc.dashweather.di

import android.content.Context
import com.kennyc.dashweather.SettingsFragment
import com.kennyc.dashweather.di.modules.DataModule
import com.kennyc.dashweather.di.modules.MiscModule
import com.kennyc.dashweather.services.WeatherDashExtension
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class, MiscModule::class])
interface AppComponent {

    fun inject(service: WeatherDashExtension)

    fun inject(fragment: SettingsFragment)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun appContext(context: Context): Builder

        @BindsInstance
        fun isDebug(@Named("isDebug") isDebug: Boolean): Builder

        fun build(): AppComponent
    }
}