package com.kennyc.dashweather.api_owm

import com.kennyc.dashweather.api_owm.response.OWMResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface OWMMapApi {

    @GET("onecall")
    fun getWeatherOneCall(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String
    ): Single<OWMResponse>
}