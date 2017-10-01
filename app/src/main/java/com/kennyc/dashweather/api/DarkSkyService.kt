package com.kennyc.dashweather.api

import com.kennyc.dashweather.BuildConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by Kenny-PC on 9/22/2017.
 */
interface DarkSkyService {
    @GET("/forecast/" + BuildConfig.API_KEY + "/{lat_lon}?exclude=flags,alerts,minutely,daily&units=us")
    fun getForecast(@Path("lat_lon") latLon: String): Call<WeatherResult>
}