package com.kennyc.dashweather.data

import com.kennyc.dashweather.data.model.WeatherLocation
import io.reactivex.Observable

interface LocationRepository {

    /**
     * Return the last known
     */
    fun getLastKnownLocation(): Observable<WeatherLocation>

    fun getLocationName(lat: Double, lon: Double): Observable<String>
}