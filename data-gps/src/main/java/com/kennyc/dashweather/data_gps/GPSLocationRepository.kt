package com.kennyc.dashweather.data_gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.model.WeatherLocation
import io.reactivex.Observable
import java.util.*

class GPSLocationRepository(context: Context) : LocationRepository {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(): Observable<WeatherLocation> {
        return Observable.create { emitter ->
            client.lastLocation
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
                    .addOnSuccessListener { location ->
                        when (location) {
                            null -> emitter.onError(NullPointerException("No Location Received"))
                            else -> {
                                emitter.onNext(WeatherLocation(location.latitude, location.longitude))
                                emitter.onComplete()
                            }
                        }
                    }
        }
    }

    override fun getLocationName(lat: Double, lon: Double): Observable<String> {
        return Observable.create { emitter ->
            val address = geocoder.getFromLocation(lat, lon, 1)

            if (address != null && address.isNotEmpty()) {
                emitter.onNext(address[0].run { "$locality, $adminArea" })
                emitter.onComplete()
            } else {
                emitter.onError(NullPointerException("No Location Name Found"))
            }
        }
    }
}