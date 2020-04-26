package com.kennyc.dashweather.data

import com.kennyc.dashweather.data.model.Weather
import io.reactivex.rxjava3.core.Observable

interface WeatherRepository {

    fun getWeather(lat: Double, lon: Double): Observable<Weather>
    
}