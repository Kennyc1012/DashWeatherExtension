package com.kennyc.dashweather.api_owm.model

import com.google.gson.annotations.SerializedName

data class OWMCurrent(@SerializedName("temp") val temp: Double,
                      @SerializedName("humidity") val humidity: Int,
                      @SerializedName("uvi") val uvIndex: Int,
                      @SerializedName("weather") val weather: OWMWeather)