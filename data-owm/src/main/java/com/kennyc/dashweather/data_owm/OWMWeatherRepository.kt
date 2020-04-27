package com.kennyc.dashweather.data_owm

import com.kennyc.dashweather.api_owm.OWMMapApi
import com.kennyc.dashweather.api_owm.response.OWMResponse
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data.model.Weather
import com.kennyc.dashweather.data.model.WeatherIcon
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class OWMWeatherRepository constructor(private val api: OWMMapApi,
                                       private val locationRepository: LocationRepository) : WeatherRepository {


    override fun getWeather(lat: Double, lon: Double, usesImperial: Boolean): Observable<Weather> {
        val units = when (usesImperial) {
            true -> "imperial"
            else -> "metric"
        }

        return api.getWeatherOneCall(lat, lon, units)
                .zipWith(locationRepository.getLocationName(lat, lon).onErrorReturn { NO_NAME }
                        , BiFunction { response, name ->

                    val nameToPass = when (name) {
                        NO_NAME -> null
                        else -> name
                    }

                    toWeather(response, nameToPass)
                })
    }

    private fun toWeather(response: OWMResponse, name: String?): Weather {
        val daily = response.daily[0].temp
        val current = response.current

        return Weather(response.lat,
                response.lon,
                current.temp,
                daily.max,
                daily.min,
                current.uvIndex.toInt(),
                current.humidity,
                current.weather[0].summary,
                name,
                toWeatherIcon(current.weather[0].icon))
    }

    private fun toWeatherIcon(icon: String): WeatherIcon = when (icon) {
        "01d" -> WeatherIcon.CLEAR
        "01n" -> WeatherIcon.CLEAR_NIGHT
        "02d", "03d" -> WeatherIcon.PARTLY_CLOUDY
        "02n", "03n" -> WeatherIcon.PARTLY_CLOUDY_NIGHT
        "04d", "04n" -> WeatherIcon.CLOUDY
        "09d" -> WeatherIcon.RAIN_SHOWERS
        "09n" -> WeatherIcon.RAIN_SHOWERS_NIGHT
        "10d" -> WeatherIcon.RAIN
        "10n" -> WeatherIcon.RAIN_NIGHT
        "11d", "11n" -> WeatherIcon.THUNDER_STORM
        "13d", "13n" -> WeatherIcon.SNOW
        "50d", "50n" -> WeatherIcon.FOG
        else -> WeatherIcon.CLEAR
    }

}

private const val NO_NAME = "ERR_NO_NAME"