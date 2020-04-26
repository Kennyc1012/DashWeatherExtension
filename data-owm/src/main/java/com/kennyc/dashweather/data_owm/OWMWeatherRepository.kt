package com.kennyc.dashweather.data_owm

import com.kennyc.dashweather.api_owm.OWMMapApi
import com.kennyc.dashweather.api_owm.response.OWMResponse
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data.model.Weather
import io.reactivex.Observable

class OWMWeatherRepository constructor(private val api: OWMMapApi) : WeatherRepository {

    override fun getWeather(lat: Double, lon: Double): Observable<Weather> {
        return api.getWeatherOneCall(lat, lon)
                .map { toWeather(it) }
    }

    private fun toWeather(response: OWMResponse): Weather {
        val daily = response.daily[0].temp
        val current = response.current

        return Weather(response.lat,
                response.lon,
                current.temp,
                daily.max,
                daily.min,
                current.uvIndex.toInt(),
                current.humidity,
                current.weather[0].summary)
    }
}