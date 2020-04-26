package com.kennyc.dashweather.data.model

data class Weather(val current: Double,
                   val high: Double,
                   val low: Double,
                   val uvIndex: Int,
                   val humidity: Int,
                   val summary: String)