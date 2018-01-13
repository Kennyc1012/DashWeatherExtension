package com.kennyc.dashweather.contracts

import android.content.Context
import com.kennyc.dashweather.api.WeatherResult
import com.kennyc.dashweather.models.DailyWeatherModel
import com.kennyc.dashweather.models.WeatherModel

open class DarkSkyContract {
    interface View {
        /**
         * Called when the required permissions are not available
         */
        fun onPermissionMissing()

        /**
         * Called when the devices location was unable to be determined
         *
         * @param exception An exception that may have been thrown in the process
         */
        fun onLocationNotFound(exception: Exception?)

        /**
         * Called when an Api response is received and the extension should display the new weather
         *
         * @param weatherResult The Api response object
         * @param usesImperial If the user is using imperial units
         */
        fun displayWeatherResult(weatherResult: WeatherResult?, usesImperial: Boolean)
    }

    interface Presenter {
        /**
         * Called when the extension is requesting an update
         *
         * @param context Application context
         * @param reason The reason the update was called
         */
        fun onRequestUpdate(context: Context, reason: Int)

        /**
         * Called when a location have been received
         *
         * @param context Application Context
         * @param latitude The latitude of the location
         * @param longitude The longitude of the location
         */
        fun onLocationReceived(context: Context, latitude: Double, longitude: Double)

        /**
         * Returns a string representation of the current High/Low temperature
         *
         * @param context Application Context
         * @param userSettings Set of strings from SharedPreferences containing user settings
         * @param model The DailyWeatherModel containing the high and low temperature
         * @param invert If the High/Low temperature should be inverted
         * @param usesImperial If the user is using imperial units
         */
        fun getHighLow(context: Context, userSettings: Set<String>, model: DailyWeatherModel?, invert: Boolean, usesImperial: Boolean): String?

        /**
         * Returns a string representation of the current humidity
         *
         * @param context Application Context
         * @param userSettings Set of strings from SharedPreferences containing user settings
         * @param model The WeatherModel containing the current humidity
         */
        fun getHumidity(context: Context, userSettings: Set<String>, model: WeatherModel?): String?

        /**
         * Returns a string representation of the current uv index
         *
         * @param context Application Context
         * @param userSettings Set of strings from SharedPreferences containing user settings
         * @param model The WeatherModel containing the current uv index
         */
        fun getUVIndex(context: Context, userSettings: Set<String>, model: WeatherModel?): String?

        /**
         * Returns a string representation of the current location EX (New York, NY)
         *
         * @param context Application Context
         * @param userSettings Set of strings from SharedPreferences containing user settings
         * @param latitude The latitude of the location
         * @param longitude The longitude of the location
         */
        fun getLocation(context: Context, userSettings: Set<String>, latitude: Float, longitude: Float): String?
    }
}