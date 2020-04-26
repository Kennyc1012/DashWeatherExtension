package com.kennyc.dashweather.data

import com.kennyc.dashweather.data.model.WeatherLocation
import io.reactivex.Flowable
import io.reactivex.Observable

interface LocationRepository {

    fun getLastKnownLocation(): Observable<WeatherLocation>

    fun requestLocationUpdates(): Observable<WeatherLocation>

    fun getLocationName(lat: Double, lon: Double): Observable<String>
}