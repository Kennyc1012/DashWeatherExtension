package com.kennyc.dashweather.data

import com.kennyc.dashweather.data.model.WeatherLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    suspend fun getLastKnownLocation(): WeatherLocation?

    suspend fun requestLocationUpdates(): Flow<WeatherLocation>

    suspend fun getLocationName(lat: Double, lon: Double): String?
}