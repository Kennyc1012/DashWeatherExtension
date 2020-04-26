package com.kennyc.dashweather.di.modules

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.kennyc.dashweather.BuildConfig
import com.kennyc.dashweather.api_owm.di.OWMComponent
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data_owm.OWMWeatherRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun providesSharedPreferences(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun providesOWMComponent(): OWMComponent = OWMComponent.builder()
            .appId(BuildConfig.API_KEY)
            .build()

    @Provides
    @Singleton
    fun providesWeatherRepository(component: OWMComponent): WeatherRepository = OWMWeatherRepository(component.api())
}