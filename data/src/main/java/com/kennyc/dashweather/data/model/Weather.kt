package com.kennyc.dashweather.data.model

data class Weather(val latitude: Double,
                   val longitude: Double,
                   val current: Double,
                   val high: Double,
                   val low: Double,
                   val humidity: Int,
                   val summary: String,
                   val locationHumanReadable: String?,
                   val icon: WeatherIcon)