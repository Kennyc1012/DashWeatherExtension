package com.kennyc.dashweather.api_owm.model

import com.google.gson.annotations.SerializedName

data class OWMDaily(@SerializedName("dt") val date: Long,
                    @SerializedName("sunrise") val sunrise: Long,
                    @SerializedName("sunset") val sunset: Long,
                    @SerializedName("weather") val weather: List<OWMWeather>,
                    @SerializedName("temp") val temp: OWMTemp)