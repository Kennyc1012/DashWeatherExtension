package com.kennyc.dashweather.api_owm

import com.kennyc.dashweather.api_owm.response.OWMResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OWMMapApi {

    @GET("onecall")
    suspend fun getWeatherOneCall(@Query("lat") lat: Double,
                                  @Query("lon") lon: Double,
                                  @Query("units") units: String): OWMResponse
}