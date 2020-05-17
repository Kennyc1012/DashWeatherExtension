package com.kennyc.dashweather.api_owm.model

import com.google.gson.annotations.SerializedName

data class OWMCurrent(@SerializedName("temp") val temp: Double,
                      @SerializedName("humidity") val humidity: Int,
                      @SerializedName("weather") val weather: List<OWMWeather>) {
    init {
        require(weather.isNotEmpty())
    }
}