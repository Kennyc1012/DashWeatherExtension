package com.kennyc.dashweather.presenters

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.text.format.DateUtils
import android.util.Log
import com.google.android.apps.dashclock.api.DashClockExtension
import com.google.android.gms.location.LocationServices
import com.kennyc.dashweather.BuildConfig
import com.kennyc.dashweather.R
import com.kennyc.dashweather.SettingsFragment
import com.kennyc.dashweather.api.ApiClient
import com.kennyc.dashweather.api.WeatherResult
import com.kennyc.dashweather.contracts.DarkSkyContract
import com.kennyc.dashweather.models.DailyWeatherModel
import com.kennyc.dashweather.models.WeatherModel
import com.kennyc.dashweather.services.DarkSkyDashExtension
import retrofit2.Response
import java.util.*
import kotlin.concurrent.thread

class DarkSkyPresenter(val view: DarkSkyContract.View) : DarkSkyContract.Presenter {

    override fun onRequestUpdate(context: Context, reason: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        if (reason == DashClockExtension.UPDATE_REASON_MANUAL || shouldUpdate(context, sharedPreferences)) {
            sharedPreferences?.edit()?.putLong(DarkSkyDashExtension.KEY_LAST_UPDATED, System.currentTimeMillis())?.apply()
            val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

            if (coarsePermission == PackageManager.PERMISSION_GRANTED || finePermission == PackageManager.PERMISSION_GRANTED) {
                val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
                fusedLocation.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationReceived(context, location.latitude, location.longitude)
                    } else {
                        view.onLocationNotFound(NullPointerException("No location received"))
                    }
                }.addOnFailureListener { exception ->
                    view.onLocationNotFound(exception)
                }
            } else {
                view.onPermissionMissing()
            }
        }
    }

    override fun onLocationReceived(context: Context, latitude: Double, longitude: Double) {
        val formattedLocation = String.format("%.4f,%.4f", latitude, longitude)
        Log.v(DarkSkyDashExtension.TAG, "Getting weather for " + formattedLocation)
        val imperial = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_use_imperial), true)
        val unit = if (imperial) "us" else "si"

        thread {
            val weatherResult = ApiClient.darkSkyService.getForecast(formattedLocation, unit).execute()
            logApiResponse(weatherResult)
            view.displayWeatherResult(weatherResult.body(), imperial)
        }
    }

    override fun getHighLow(context: Context, userSettings: Set<String>, model: DailyWeatherModel?, invert: Boolean, usesImperial: Boolean): String? {
        if (userSettings.contains(SettingsFragment.WEATHER_DETAILS_HIGH_LOW)) {
            val high: String
            val low: String

            if (model != null) {
                val tempHigh = Math.round(model.data?.get(0)!!.temperatureHigh)
                val tempLow = Math.round(model.data?.get(0)!!.temperatureLow)
                high = context.getString(if (usesImperial) R.string.temp_F else R.string.temp_C, tempHigh)
                low = context.getString(if (usesImperial) R.string.temp_F else R.string.temp_C, tempLow)
            } else {
                high = "???"
                low = "???"
            }

            val stringResource = if (invert) R.string.high_low_invert else R.string.high_low
            return context.getString(stringResource, if (invert) low else high, if (invert) high else low)
        }

        return null
    }

    override fun getHumidity(context: Context, userSettings: Set<String>, model: WeatherModel?): String? {
        if (userSettings.contains(SettingsFragment.WEATHER_DETAILS_HUMIDITY)) {

            return if (model != null) {
                val humidity = Math.round(model.humidity * 100)
                context.getString(R.string.humidity, humidity) + "%"
            } else {
                "???"
            }
        }

        return null
    }

    override fun getUVIndex(context: Context, userSettings: Set<String>, model: WeatherModel?): String? {
        if (userSettings.contains(SettingsFragment.WEATHER_DETAILS_UV_INDEX)) {
            return if (model != null) {
                context.getString(R.string.uv_index, model.uvIndex)
            } else {
                "???"
            }
        }

        return null
    }

    override fun getLocation(context: Context, userSettings: Set<String>, latitude: Float, longitude: Float): String? {
        if (userSettings.contains(SettingsFragment.WEATHER_DETAILS_LOCATION)) {
            val address = Geocoder(context, Locale.getDefault()).getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)

            return if (address != null && !address.isEmpty()) {
                val localAddress = address[0]
                localAddress.locality + ", " + localAddress.adminArea
            } else {
                "???"
            }
        }

        return null
    }

    /**
     * Returns if the extension should updated based on user settings
     *
     * @param context Application context
     * @param sharedPreferences User settings to determine if the extension should update
     */
    private fun shouldUpdate(context: Context, sharedPreferences: SharedPreferences): Boolean {
        // Check if data saver is on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (connMgr.isActiveNetworkMetered && connMgr.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED) {
                Log.v(DarkSkyDashExtension.TAG, "Data saver enabled and on a metered network, skipping update")
                return false
            }
        }

        // Check if battery saver is enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (powerManager.isPowerSaveMode) {
                Log.v(DarkSkyDashExtension.TAG, "Battery saver enabled, skipping update")
                return false
            }
        }

        val currentTime = System.currentTimeMillis()
        val updateFrequency = sharedPreferences.getString(context.getString(R.string.pref_key_update_frequency), SettingsFragment.UPDATE_FREQUENCY_1_HOUR)
        val lastUpdate = sharedPreferences.getLong(DarkSkyDashExtension.KEY_LAST_UPDATED, 0)

        when (updateFrequency) {
            SettingsFragment.UPDATE_FREQUENCY_NO_LIMIT -> return true
            SettingsFragment.UPDATE_FREQUENCY_1_HOUR -> return currentTime - lastUpdate > DateUtils.HOUR_IN_MILLIS
            SettingsFragment.UPDATE_FREQUENCY_3_HOURS -> return currentTime - lastUpdate > DateUtils.HOUR_IN_MILLIS * 3
            SettingsFragment.UPDATE_FREQUENCY_4_HOURS -> return currentTime - lastUpdate > DateUtils.HOUR_IN_MILLIS * 4
        }

        return false
    }

    private fun logApiResponse(response: Response<WeatherResult>) {
        if (BuildConfig.DEBUG) {
            val headers = response.headers()
            val headerNames = headers.names()
            for (name in headerNames) Log.i(DarkSkyDashExtension.TAG, name + ": " + headers.get(name))
            Log.i(DarkSkyDashExtension.TAG, "Response Code: " + response.code())
        }
    }
}