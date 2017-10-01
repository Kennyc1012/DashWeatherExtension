package com.kennyc.dashweather.services

import android.location.Geocoder
import com.google.android.apps.dashclock.api.DashClockExtension
import com.google.android.apps.dashclock.api.ExtensionData
import com.kennyc.dashweather.BuildConfig
import com.kennyc.dashweather.R
import com.kennyc.dashweather.api.ApiClient
import java.util.*


/**
 * Created by Kenny-PC on 9/22/2017.
 */
class DarkSkyDashExtension : DashClockExtension() {


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
        // TODO Allow dynamic location
        val weatherResult = ApiClient.darkSkyService.getForecast(String.format("%.4f,%.4f", BuildConfig.LATITUDE, BuildConfig.LONGITUDE)).execute()
        val current = weatherResult.body()?.currently
        val daily = weatherResult.body()?.daily
        val currentTemp: String
        val iconDrawable: Int
        val currentCondition: String

        if (current != null) {
            // TODO Determine temperature units
            val temp = Math.round(current.temperature)
            currentTemp = getString(R.string.temp_F, temp)
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
            val weahter = daily.data!!.get(0)
            val tempHigh = Math.round(weahter.temperatureHigh)
            val tempLow = Math.round(weahter.temperatureLow)
            val humidityConversion = Math.round(weahter.humidity * 100)
            high = getString(R.string.temp_F, tempHigh)
            low = getString(R.string.temp_F, tempLow)
            humidity = getString(R.string.humidty, humidityConversion)
        } else {
            high = "???"
            low = "???"
            humidity = "???"
        }

        val location: String
        val address = Geocoder(applicationContext, Locale.getDefault()).getFromLocation(BuildConfig.LATITUDE, BuildConfig.LONGITUDE, 1)

        if (address != null && !address.isEmpty()) {
            val localAddress = address[0]
            location = localAddress.locality + ", " + localAddress.adminArea
        } else {
            location = "???"
        }

        publishUpdate(ExtensionData()
                .visible(true)
                .icon(iconDrawable)
                .status(currentTemp)
                .expandedTitle(currentTemp + " - " + currentCondition)
                .expandedBody(getString(R.string.high_low, high, low)
                        + "\n" + humidity
                        + "\n" + location))
    }
}