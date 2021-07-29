package com.kennyc.dashweather.data

import com.kennyc.dashweather.data.model.Weather
import io.reactivex.rxjava3.core.Single

interface WeatherRepository {

    fun getWeather(lat: Double, lon: Double, usesImperial: Boolean): Single<Weather>

    fun getWeatherProviderName(): String
}