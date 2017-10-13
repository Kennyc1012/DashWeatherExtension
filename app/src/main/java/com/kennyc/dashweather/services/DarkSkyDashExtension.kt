package com.kennyc.dashweather.services

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.preference.PreferenceManager
import android.support.annotation.Nullable
import android.support.v4.content.ContextCompat
import android.text.format.DateUtils
import android.util.Log
import com.google.android.apps.dashclock.api.DashClockExtension
import com.google.android.apps.dashclock.api.ExtensionData
import com.google.android.gms.location.LocationServices
import com.kennyc.dashweather.BuildConfig
import com.kennyc.dashweather.R
import com.kennyc.dashweather.SettingsActivity
import com.kennyc.dashweather.SettingsFragment
import com.kennyc.dashweather.api.ApiClient
import com.kennyc.dashweather.api.WeatherResult
import com.kennyc.dashweather.models.DailyWeatherModel
import com.kennyc.dashweather.models.WeatherModel
import retrofit2.Response
import java.util.*
import kotlin.concurrent.thread


/**
 * Created by Kenny-PC on 9/22/2017.
 */
class DarkSkyDashExtension : DashClockExtension() {
    companion object {
        const val KEY_LAST_UPDATED = "com.kennyc.dashweather.LAST_UPDATE"
        const val TAG = "DarkSkyDashExtension"
        const val INTENT_ACTION = "com.kennyc.dashweather.REFRESH"

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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        if (reason == UPDATE_REASON_MANUAL || shouldUpdate(sharedPreferences)) {
            sharedPreferences.edit().putLong(KEY_LAST_UPDATED, System.currentTimeMillis()).apply()
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
    }

    private fun onLocationReceived(latitude: Double, longitude: Double) {
        val formattedLocation = String.format("%.4f,%.4f", latitude, longitude)
        Log.v(TAG, "Getting weather for " + formattedLocation)

        thread {
            val imperial = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(getString(R.string.pref_key_use_imperial), true)
            val unit = if (imperial) "us" else "si"
            val weatherResult = ApiClient.darkSkyService.getForecast(formattedLocation, unit).execute()
            logApiResponse(weatherResult)
            onApiResponse(weatherResult.body(), imperial)
        }
    }

    private fun onApiResponse(weatherResult: WeatherResult?, usesImperial: Boolean) {
        val current = weatherResult?.currently
        val daily = weatherResult?.daily
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val uiPreferences = sharedPreferences.getStringSet(getString(R.string.pref_key_details),
                setOf(SettingsFragment.WEATHER_DETAILS_HIGH_LOW, SettingsFragment.WEATHER_DETAILS_LOCATION))
        val currentTemp: String
        val iconDrawable: Int
        val currentCondition: String

        if (current != null) {
            val temp = Math.round(current.temperature)
            currentTemp = getString(if (usesImperial) R.string.temp_F else R.string.temp_C, temp)
            iconDrawable = current.getIconDrawable()
            currentCondition = current.summary
        } else {
            currentTemp = "???"
            iconDrawable = R.drawable.ic_weather_sunny_black_24dp
            currentCondition = "???"
        }

        val expandedBody = StringBuilder()
        val highLow = getHighLow(uiPreferences, sharedPreferences.getBoolean(getString(R.string.pref_key_invert_high_low), false), daily, usesImperial)
        if (highLow != null) expandedBody.append(highLow)

        val humidity = getHumidity(uiPreferences, current)
        if (humidity != null) {
            if (expandedBody.isNotEmpty()) expandedBody.append("\n")
            expandedBody.append(humidity)
        }

        val uvIndex = getUVIndex(uiPreferences, current)
        if (uvIndex != null) {
            if (expandedBody.isNotEmpty()) expandedBody.append("\n")
            expandedBody.append(uvIndex)
        }

        // TODO Better check this
        val location = getLastLocation(uiPreferences, weatherResult!!.latitude, weatherResult!!.longitude)
        if (location != null) {
            if (expandedBody.isNotEmpty()) expandedBody.append("\n")
            expandedBody.append(location)
        }

        val body = expandedBody.toString()

        publishUpdate(ExtensionData()
                .visible(true)
                .clickIntent(Intent(INTENT_ACTION))
                .icon(iconDrawable)
                .status(currentTemp)
                .expandedTitle(currentTemp + " - " + currentCondition)
                .expandedBody(body))
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

    private fun logApiResponse(response: Response<WeatherResult>) {
        if (BuildConfig.DEBUG) {
            val headers = response.headers()
            val headerNames = headers.names()
            for (name in headerNames) Log.i(TAG, name + ": " + headers.get(name))
            Log.i(TAG, "Response Code: " + response.code())
        }
    }

    private fun shouldUpdate(sharedPreferences: SharedPreferences): Boolean {
        // Check if data saver is on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connMgr = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (connMgr.isActiveNetworkMetered && connMgr.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED) {
                Log.v(TAG, "Data saver enabled and on a metered network, skipping update")
                return false
            }
        }

        // Check if battery saver is enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (powerManager.isPowerSaveMode) {
                Log.v(TAG, "Battery saver enabled, skipping update")
                return false
            }
        }

        val currentTime = System.currentTimeMillis()
        val updateFrequency = sharedPreferences.getString(getString(R.string.pref_key_update_frequency), SettingsFragment.UPDATE_FREQUENCY_1_HOUR)
        val lastUpdate = sharedPreferences.getLong(KEY_LAST_UPDATED, 0)

        when (updateFrequency) {
            SettingsFragment.UPDATE_FREQUENCY_NO_LIMIT -> return true
            SettingsFragment.UPDATE_FREQUENCY_1_HOUR -> return currentTime - lastUpdate > DateUtils.HOUR_IN_MILLIS
            SettingsFragment.UPDATE_FREQUENCY_3_HOURS -> return currentTime - lastUpdate > DateUtils.HOUR_IN_MILLIS * 3
            SettingsFragment.UPDATE_FREQUENCY_4_HOURS -> return currentTime - lastUpdate > DateUtils.HOUR_IN_MILLIS * 4
        }

        return false
    }

    @Nullable
    private fun getLastLocation(set: Set<String>, latitude: Float, longitude: Float): String? {
        if (set.contains(SettingsFragment.WEATHER_DETAILS_LOCATION)) {
            val location: String
            val address = Geocoder(applicationContext, Locale.getDefault()).getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)

            when (address != null && !address.isEmpty()) {
                true -> {
                    val localAddress = address[0]
                    return localAddress.locality + ", " + localAddress.adminArea
                }

                false -> return "???"
            }
        }

        return null
    }

    @Nullable
    private fun getHighLow(set: Set<String>, invert: Boolean, model: DailyWeatherModel?, usesImperial: Boolean): String? {
        if (set.contains(SettingsFragment.WEATHER_DETAILS_HIGH_LOW)) {
            val high: String
            val low: String

            if (model != null) {
                val tempHigh = Math.round(model.data?.get(0)!!.temperatureHigh)
                val tempLow = Math.round(model.data?.get(0)!!.temperatureLow)
                high = getString(if (usesImperial) R.string.temp_F else R.string.temp_C, tempHigh)
                low = getString(if (usesImperial) R.string.temp_F else R.string.temp_C, tempLow)
            } else {
                high = "???"
                low = "???"
            }

            val stringResource = if (invert) R.string.high_low_invert else R.string.high_low
            return getString(stringResource, if (invert) low else high, if (invert) high else low)
        }

        return null
    }

    @Nullable
    private fun getHumidity(set: Set<String>, model: WeatherModel?): String? {
        if (set.contains(SettingsFragment.WEATHER_DETAILS_HUMIDITY)) {

            return if (model != null) {
                val humidity = Math.round(model.humidity * 100)
                getString(R.string.humidity, humidity) + "%"
            } else {
                "???"
            }
        }

        return null
    }

    @Nullable
    private fun getUVIndex(set: Set<String>, model: WeatherModel?): String? {
        if (set.contains(SettingsFragment.WEATHER_DETAILS_UV_INDEX)) {
            return if (model != null) {
                getString(R.string.uv_index, model.uvIndex)
            } else {
                "???"
            }
        }

        return null
    }

    inner class DashClockReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onUpdateData(DashClockExtension.UPDATE_REASON_MANUAL)
        }
    }
}