package com.kennyc.dashweather.di.modules

import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data_owm.OWMWeatherRepository
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindsWeatherRepository(repository: OWMWeatherRepository): WeatherRepository
}