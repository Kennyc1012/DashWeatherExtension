package com.kennyc.dashweather.services

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.text.format.DateUtils
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.apps.dashclock.api.DashClockExtension
import com.google.android.apps.dashclock.api.ExtensionData
import com.kennyc.dashweather.R
import com.kennyc.dashweather.SettingsActivity
import com.kennyc.dashweather.SettingsFragment
import com.kennyc.dashweather.WeatherApp
import com.kennyc.dashweather.data.contract.WeatherContract
import com.kennyc.dashweather.data.model.Weather
import com.kennyc.dashweather.presenters.WeatherPresenter
import javax.inject.Inject
import kotlin.math.roundToInt


/**
 * Created by Kenny-PC on 9/22/2017.
 */
class WeatherDashExtension : DashClockExtension(), WeatherContract.View {
    companion object {
        const val KEY_LAST_UPDATED = "com.kennyc.dashweather.LAST_UPDATE"
        const val TAG = "DarkSkyDashExtension"
        const val INTENT_ACTION = "com.kennyc.dashweather.REFRESH"
        const val EXTRA_LATITUDE = "com.kennyc.dashweather.EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "com.kennyc.dashweather.EXTRA_LONGITUDE"

        fun sendBroadcast(context: Context) {
            context.sendBroadcast(Intent(INTENT_ACTION))
        }
    }

    private var broadcastReceiver: BroadcastReceiver? = null

    @Inject
    lateinit var presenter: WeatherPresenter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onInitialize(isReconnect: Boolean) {
        Log.v(TAG, "onInitialize")
        super.onInitialize(isReconnect)
        (applicationContext as WeatherApp).component.inject(this)
        presenter.setView(this)

        broadcastReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to unregister receiver", ex)
            }
        }

        broadcastReceiver = DashClockReceiver()
        registerReceiver(broadcastReceiver, IntentFilter(INTENT_ACTION))
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        presenter.onDestroy()
        broadcastReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to unregister receiver:", ex)
            }
        }
        super.onDestroy()
    }

    override fun onUpdateData(reason: Int) {
        Log.v(TAG, "onUpdateData - reason: $reason")
        presenter.requestUpdate(reason)
    }

    override fun canUpdate(): Boolean {
        val context = applicationContext

        // Check if data saver is on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (connMgr.isActiveNetworkMetered && connMgr.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED) {
                //  Log.v(DarkSkyDashExtension.TAG, "Data saver enabled and on a metered network, skipping update")
                return false
            }
        }

        // Check if battery saver is enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (powerManager.isPowerSaveMode) {
                // Log.v(DarkSkyDashExtension.TAG, "Battery saver enabled, skipping update")
                return false
            }
        }

        val currentTime = System.currentTimeMillis()
        val updateFrequency = sharedPreferences.getString(SettingsActivity.KEY_UPDATE_FREQUENCY, SettingsFragment.UPDATE_FREQUENCY_1_HOUR)
        val lastUpdate = sharedPreferences.getLong(KEY_LAST_UPDATED, 0)

        when (updateFrequency) {
            SettingsFragment.UPDATE_FREQUENCY_NO_LIMIT -> return true
            SettingsFragment.UPDATE_FREQUENCY_1_HOUR -> return currentTime - lastUpdate > DateUtils.HOUR_IN_MILLIS
            SettingsFragment.UPDATE_FREQUENCY_3_HOURS -> return currentTime - lastUpdate > DateUtils.HOUR_IN_MILLIS * 3
            SettingsFragment.UPDATE_FREQUENCY_4_HOURS -> return currentTime - lastUpdate > DateUtils.HOUR_IN_MILLIS * 4
        }

        return false
    }

    override fun hasRequiredPermissions(): Boolean {
        val coarsePermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        val finePermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
        return coarsePermission == PackageManager.PERMISSION_GRANTED || finePermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onPermissionsRequired() {
        publishUpdate(ExtensionData()
                .visible(true)
                .icon(R.drawable.ic_map_marker_off_black_24dp)
                .status(getString(R.string.permission_title))
                .expandedTitle(getString(R.string.permission_title))
                .expandedBody(getString(R.string.permission_body))
                .clickIntent(SettingsActivity.createIntent(applicationContext, true)))
    }

    override fun onWeatherReceived(weather: Weather) {
        val expandedBody = StringBuilder()

        val currentTemp = weather.current.roundToInt()
        // TODO Support F
        val currentTempString = getString(R.string.temp_C, currentTemp)
        // TODO Icon
        // iconDrawable = current.getIconDrawable()
        val currentCondition = weather.summary

        val userSettings = sharedPreferences.getStringSet(SettingsActivity.KEY_SHOW_WEATHER_DETAILS,
                setOf(SettingsFragment.WEATHER_DETAILS_HIGH_LOW, SettingsFragment.WEATHER_DETAILS_LOCATION))

        //High/Low
        if (userSettings?.contains(SettingsFragment.WEATHER_DETAILS_HIGH_LOW) == true) {
            val invert = sharedPreferences.getBoolean(SettingsActivity.KEY_INVERT_HIGH_LOW, false)
            val invertResource = if (invert) R.string.high_low_invert else R.string.high_low
            val tempHigh = weather.high.roundToInt()
            val tempLow = weather.low.roundToInt()
            // TODO Support F
            val high = getString(R.string.temp_C, tempHigh)
            val low = getString(R.string.temp_C, tempLow)

            expandedBody.append(getString(invertResource, if (invert) low else high, if (invert) high else low))
        }


        // Humidity
        if (userSettings?.contains(SettingsFragment.WEATHER_DETAILS_HUMIDITY) == true) {
            if (expandedBody.isNotEmpty()) expandedBody.append("\n")
            expandedBody.append(getString(R.string.humidity, weather.humidity))
                    .append("%")
        }

        // UV Index
        if (userSettings?.contains(SettingsFragment.WEATHER_DETAILS_UV_INDEX) == true) {
            if (expandedBody.isNotEmpty()) expandedBody.append("\n")
            expandedBody.append(getString(R.string.uv_index, weather.uvIndex))
        }

        // Location
        weather.locationHumanReadable?.let { location ->
            if (expandedBody.isNotEmpty()) expandedBody.append("\n")
            expandedBody.append(location)
        }

        val body = expandedBody.toString()

        publishUpdate(ExtensionData()
                .visible(true)
                .clickIntent(Intent(INTENT_ACTION))
                /*TODO.icon(iconDrawable)*/
                .status(currentTempString)
                .expandedTitle("$currentTempString - $currentCondition")
                .expandedBody(body))
    }

    override fun onWeatherFailure(error: Throwable) {
        Log.e(TAG, "unable to get location", error)
        publishUpdate(ExtensionData()
                .visible(true)
                .icon(R.drawable.ic_map_marker_off_black_24dp)
                .status(getString(R.string.error_no_location))
                .expandedTitle(getString(R.string.error_no_location)))
    }


    inner class DashClockReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.hasExtra(EXTRA_LATITUDE) && intent.hasExtra(EXTRA_LATITUDE)) {
                val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
                val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
                //  presenter.onLocationReceived(applicationContext, latitude, longitude)
            } else {
                onUpdateData(DashClockExtension.UPDATE_REASON_MANUAL)
            }
        }
    }
}