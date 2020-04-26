package com.kennyc.dashweather.api_owm.response

import com.google.gson.annotations.SerializedName
import com.kennyc.dashweather.api_owm.model.OWMCurrent
import com.kennyc.dashweather.api_owm.model.OWMDaily

data class OWMResponse(@SerializedName("lat") val lat: Double,
                       @SerializedName("lon") val lon: Double,
                       @SerializedName("current") val current: OWMCurrent,
                       @SerializedName("daily") val daily: List<OWMDaily>) {
    init {
        require(daily.isNotEmpty())
    }
}