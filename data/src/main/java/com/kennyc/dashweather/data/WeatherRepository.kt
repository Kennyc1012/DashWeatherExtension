package com.kennyc.dashweather.data

import com.kennyc.dashweather.data.model.Weather

interface WeatherRepository {

    suspend fun getWeather(lat: Double, lon: Double, usesImperial: Boolean): Weather

    fun getWeatherProviderName(): String
}