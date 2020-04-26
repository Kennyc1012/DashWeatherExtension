package com.kennyc.dashweather.di.modules

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.kennyc.dashweather.BuildConfig
import com.kennyc.dashweather.api_owm.di.OWMComponent
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data.contract.WeatherContract
import com.kennyc.dashweather.data.model.LocalPreferences
import com.kennyc.dashweather.data_gps.GPSLocationRepository
import com.kennyc.dashweather.data_owm.OWMWeatherRepository
import com.kennyc.dashweather.data_shared_preferences.SharedPreferenceProvider
import com.kennyc.dashweather.presenters.WeatherPresenter
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
    fun providesWeatherRepository(component: OWMComponent, locationRepository: LocationRepository): WeatherRepository =
            OWMWeatherRepository(component.api(), locationRepository)

    @Provides
    @Singleton
    fun providesLocationRepository(context: Context): LocationRepository = GPSLocationRepository(context)

    @Provides
    @Singleton
    fun providesLocalPreferences(preferences: SharedPreferences): LocalPreferences = SharedPreferenceProvider(preferences)

    @Provides
    fun providesPresenter(weatherRepository: WeatherRepository,
                          locationRepository: LocationRepository,
                          preferences: LocalPreferences): WeatherContract.Presenter =
            WeatherPresenter(weatherRepository, locationRepository, preferences)
}