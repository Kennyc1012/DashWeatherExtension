package com.kennyc.dashweather.data_gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import android.text.format.DateUtils
import com.google.android.gms.location.*
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.model.WeatherLocation
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import java.util.*

class GPSLocationRepository(context: Context) : LocationRepository {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(): Maybe<WeatherLocation> {
        return Maybe.create { emitter ->
            client.lastLocation.addOnFailureListener {
                emitter.onError(it)
            }.addOnSuccessListener {
                if (it != null) {
                    emitter.onSuccess(WeatherLocation(it.latitude, it.longitude))
                } else {
                    emitter.onError(NullPointerException("No Location found"))
                }
            }
        }
    }

    override fun getLocationName(lat: Double, lon: Double): Maybe<String> {
        val address = geocoder.getFromLocation(lat, lon, 1)

        return if (address != null && address.isNotEmpty()) {
            address[0].run { "$locality, $adminArea" }.let {
                Maybe.just(it)
            }
        } else {
            Maybe.empty()
        }
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(): Observable<WeatherLocation> {
        return Observable.create { emitter ->
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
                            emitter.onNext(WeatherLocation(location.latitude, location.longitude))
                        }
                    }

                    client.removeLocationUpdates(this)
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    if (!availability.isLocationAvailable) {
                        emitter.onError(IllegalStateException("Location not available"))
                        client.removeLocationUpdates(this)
                    }
                }
            }

            client.requestLocationUpdates(request, cb, Looper.getMainLooper())

        }
    }
}