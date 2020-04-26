package com.kennyc.dashweather.data

import com.kennyc.dashweather.data.model.Weather
import io.reactivex.Observable

interface WeatherRepository {

    fun getWeather(lat: Double, lon: Double, usesImperial: Boolean): Observable<Weather>

}