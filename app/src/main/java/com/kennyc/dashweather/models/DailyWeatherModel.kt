package com.kennyc.dashweather.models

/**
 * Created by Kenny-PC on 9/22/2017.
 */
open class DailyWeatherModel {
    open var summary: String = ""

    open var icon: String? = null

    open var data: List<WeatherModel>? = null
}