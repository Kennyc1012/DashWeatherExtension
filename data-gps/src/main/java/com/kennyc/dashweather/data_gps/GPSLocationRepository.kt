package com.kennyc.dashweather.data_gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.text.format.DateUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.model.WeatherLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class GPSLocationRepository(context: Context) : LocationRepository {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): WeatherLocation? {
        return when (val location: Location? = client.lastLocation.await()) {
            null -> null
            else -> WeatherLocation(location.latitude, location.longitude)
        }
    }

    override suspend fun getLocationName(lat: Double, lon: Double): String? {
        val address = geocoder.getFromLocation(lat, lon, 1)

        return if (address != null && address.isNotEmpty()) {
            address[0].run { "$locality, $adminArea" }
        } else {
            null
        }
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    override suspend fun requestLocationUpdates(): Flow<WeatherLocation> = channelFlow {
        val request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setExpirationDuration(DateUtils.MINUTE_IN_MILLIS)
                .setNumUpdates(1)

        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                when (result) {
                    null -> throw NullPointerException("No location received from updates")
                    else -> {
                        val location = result.locations[0]
                        trySend(WeatherLocation(location.latitude, location.longitude))
                    }
                }

                client.removeLocationUpdates(this)
            }
        }

        client.requestLocationUpdates(request, cb, Looper.getMainLooper()).await()
        awaitClose { client.removeLocationUpdates(cb) }
    }
}