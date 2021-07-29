package com.kennyc.dashweather.data

import com.kennyc.dashweather.data.model.WeatherLocation
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable

interface LocationRepository {

    fun getLastKnownLocation(): Maybe<WeatherLocation>

    fun requestLocationUpdates(): Observable<WeatherLocation>

    fun getLocationName(lat: Double, lon: Double): Maybe<String>
}