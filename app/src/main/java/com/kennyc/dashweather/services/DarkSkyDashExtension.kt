package com.kennyc.dashweather.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.apps.dashclock.api.DashClockExtension
import com.google.android.apps.dashclock.api.ExtensionData
import com.google.android.gms.location.LocationServices
import com.kennyc.dashweather.R
import com.kennyc.dashweather.SettingsActivity
import com.kennyc.dashweather.api.ApiClient
import com.kennyc.dashweather.api.WeatherResult
import java.util.*
import kotlin.concurrent.thread


/**
 * Created by Kenny-PC on 9/22/2017.
 */
class DarkSkyDashExtension : DashClockExtension() {
    companion object {
        const val TAG = "DarkSkyDashExtension"
        const val INTENT_ACTION = "com.kennyc.dashweather.refresh"

        fun sendBroadcast(context: Context) {
            context.sendBroadcast(Intent(INTENT_ACTION))
        }
    }

    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onInitialize(isReconnect: Boolean) {
        Log.v(TAG, "onInitialize")
        super.onInitialize(isReconnect)
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver)
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to unregister receiver", ex)
            }
        }

        broadcastReceiver = DashClockReceiver()
        registerReceiver(broadcastReceiver, IntentFilter(INTENT_ACTION))
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        super.onDestroy()
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver)
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to unregister receiver:", ex)
            }
        }
    }

    /**
     * Called when the DashClock app process is requesting that the extension provide updated
     * information to show to the user. Implementations can choose to do nothing, or more commonly,
     * provide an update using the [.publishUpdate] method. Note that doing
     * nothing doesn't clear existing data. To clear any existing data, call
     * [.publishUpdate] with `null` data.
     *
     * @param reason The reason for the update. See [.UPDATE_REASON_PERIODIC] and related
     * constants for more details.
     */
    override fun onUpdateData(reason: Int) {
        Log.v(TAG, "onUpdateData - reason: " + reason)
        val coarsePermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        val finePermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)

        if (coarsePermission == PackageManager.PERMISSION_GRANTED || finePermission == PackageManager.PERMISSION_GRANTED) {
            val fusedLocation = LocationServices.getFusedLocationProviderClient(applicationContext)
            fusedLocation.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                } else {
                    onLocationFailed(NullPointerException("No location received"))
                }
            }.addOnFailureListener { exception ->
                onLocationFailed(exception)
            }
        } else {
            onPermissionMissing()
        }
    }

    private fun onLocationReceived(latitude: Double, longitude: Double) {
        val formattedLocation = String.format("%.4f,%.4f", latitude, longitude)
        Log.v(TAG, "Getting weather for " + formattedLocation)

        thread {
            val imperial = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(getString(R.string.pref_key_use_imperial), true)
            val unit = if (imperial) "us" else "si"
            val weatherResult = ApiClient.darkSkyService.getForecast(formattedLocation, unit).execute()
            onApiResponse(weatherResult.body(), imperial)
        }
    }

    private fun onApiResponse(weatherResult: WeatherResult?, usesImerpal: Boolean) {
        val current = weatherResult?.currently
        val daily = weatherResult?.daily
        val currentTemp: String
        val iconDrawable: Int
        val currentCondition: String

        if (current != null) {
            val temp = Math.round(current.temperature)
            currentTemp = getString(if (usesImerpal) R.string.temp_F else R.string.temp_C, temp)
            iconDrawable = current.getIconDrawable()
            currentCondition = current.summary
        } else {
            currentTemp = "???"
            iconDrawable = R.drawable.ic_weather_sunny_black_24dp
            currentCondition = "???"
        }

        val high: String
        val low: String
        val humidity: String

        if (!daily?.data?.isEmpty()!!) {
            val weather = daily.data!!.get(0)
            val tempHigh = Math.round(weather.temperatureHigh)
            val tempLow = Math.round(weather.temperatureLow)
            val humidityConversion = Math.round(weather.humidity * 100)
            high = getString(if (usesImerpal) R.string.temp_F else R.string.temp_C, tempHigh)
            low = getString(if (usesImerpal) R.string.temp_F else R.string.temp_C, tempLow)
            humidity = getString(R.string.humidity, humidityConversion) + "%"
        } else {
            high = "???"
            low = "???"
            humidity = "???"
        }

        val location: String
        val address = Geocoder(applicationContext, Locale.getDefault()).getFromLocation(weatherResult?.latitude?.toDouble(), weatherResult?.longitude?.toDouble(), 1)

        when (address != null && !address.isEmpty()) {
            true -> {
                val localAddress = address[0]
                location = localAddress.locality + ", " + localAddress.adminArea
            }

            false -> location = "???"
        }

        publishUpdate(ExtensionData()
                .visible(true)
                .clickIntent(Intent(INTENT_ACTION))
                .icon(iconDrawable)
                .status(currentTemp)
                .expandedTitle(currentTemp + " - " + currentCondition)
                .expandedBody(getString(R.string.high_low, high, low)
                        + "\n" + humidity
                        + "\n" + location))
    }

    private fun onLocationFailed(exception: Exception) {
        Log.e(TAG, "unable to get location", exception)
        publishUpdate(ExtensionData()
                .visible(true)
                .icon(R.drawable.ic_map_marker_off_black_24dp)
                .status(getString(R.string.error_no_location))
                .expandedTitle(getString(R.string.error_no_location))
                .expandedBody(getString(R.string.error_no_location_desc)))
    }

    private fun onPermissionMissing() {
        publishUpdate(ExtensionData()
                .visible(true)
                .icon(R.drawable.ic_map_marker_off_black_24dp)
                .status(getString(R.string.permission_title))
                .expandedTitle(getString(R.string.permission_title))
                .expandedBody(getString(R.string.permission_body))
                .clickIntent(SettingsActivity.createIntent(applicationContext, true)))
    }

    inner class DashClockReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onUpdateData(DashClockExtension.UPDATE_REASON_MANUAL)
        }
    }
}