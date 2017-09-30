package com.kennyc.dashweather.api

import com.kennyc.dashweather.models.DailyWeatherModel
import com.kennyc.dashweather.models.WeatherModel

/**
 * Created by Kenny-PC on 9/22/2017.
 */
open class WeatherResult {
    open var latitude: Float = 0.0F

    open var longitude: Float = 0.0F

    open var currently: WeatherModel? = null

    open var daily: DailyWeatherModel? = null
}