package com.kennyc.dashweather.data_owm

import com.kennyc.dashweather.api_owm.OWMMapApi
import com.kennyc.dashweather.api_owm.response.OWMResponse
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data.model.Weather
import com.kennyc.dashweather.data.model.WeatherIcon
import io.reactivex.rxjava3.core.Single

class OWMWeatherRepository constructor(
    private val api: OWMMapApi,
    private val locationRepository: LocationRepository
) : WeatherRepository {

    override fun getWeather(lat: Double, lon: Double, usesImperial: Boolean): Single<Weather> {
        val units = when (usesImperial) {
            true -> "imperial"
            else -> "metric"
        }

        return locationRepository.getLocationName(lat, lon)
            .flatMapSingle { name ->
                api.getWeatherOneCall(lat, lon, units)
                    .map { toWeather(it, name) }
            }
            .switchIfEmpty(api.getWeatherOneCall(lat, lon, units)
                .map { toWeather(it, null) })
    }

    private fun toWeather(response: OWMResponse, name: String?): Weather {
        val daily = response.daily[0].temp
        val current = response.current

        return Weather(
            response.lat,
            response.lon,
            current.temp,
            daily.max,
            daily.min,
            current.humidity,
            current.weather[0].summary,
            name,
            toWeatherIcon(current.weather[0].icon)
        )
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

    override fun getWeatherProviderName(): String = "OpenWeather"
}

private const val NO_NAME = "ERR_NO_NAME"