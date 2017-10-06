package com.kennyc.dashweather.models

import android.support.annotation.DrawableRes
import com.kennyc.dashweather.R

/**
 * Created by Kenny-PC on 9/22/2017.
 */
open class WeatherModel {
    open var time: Long = 0

    open var temperature: Double = 0.0

    open var temperatureHigh: Double = 0.0

    open var temperatureLow: Double = 0.0

    open var humidity: Float = 0.0f

    open var uvIndex: Int = 0

    open var icon: String? = null

    open var summary: String = ""

    @DrawableRes
    fun getIconDrawable(): Int {
        if (icon != null) {
            when (icon) {
                "clear-day" -> return R.drawable.ic_weather_sunny_black_24dp
                "clear-night" -> return R.drawable.ic_weather_night_black_24dp
                "rain" -> return R.drawable.ic_weather_rainy_black_24dp
                "snow" -> return R.drawable.ic_weather_snowy_black_24dp
                "sleet" -> return R.drawable.ic_weather_snowy_black_24dp
                "wind" -> return R.drawable.ic_weather_windy_black_24dp
                "fog" -> return R.drawable.ic_weather_fog_black_24dp
                "cloudy" -> return R.drawable.ic_weather_cloudy_black_24dp
                "partly-cloudy-day" -> return R.drawable.ic_weather_partlycloudy_black_24dp
                "partly-cloudy-night" -> return R.drawable.ic_weather_partlycloudy_black_24dp
                "hail" -> return R.drawable.ic_weather_hail_black_24dp
                "thunderstorm" -> return R.drawable.ic_weather_lightning_black_24dp
            }
        }

        return R.drawable.ic_weather_partlycloudy_black_24dp
    }
}