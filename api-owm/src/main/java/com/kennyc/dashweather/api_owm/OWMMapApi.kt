package com.kennyc.dashweather.api_owm

import com.kennyc.dashweather.api_owm.response.OWMResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface OWMMapApi {

    @GET("/onecall?lat={lat}&lon={lon}&units=metric")
    fun getWeatherOneCall(@Query("lat") lat: Double, @Query("lon") lon: Double): Observable<OWMResponse>
}