package com.kennyc.dashweather.api_owm.model

import com.google.gson.annotations.SerializedName

data class OWMWeather(@SerializedName("description") val summary: String,
                      @SerializedName("icon") val icon: String,
                      @SerializedName("main") val main: String)