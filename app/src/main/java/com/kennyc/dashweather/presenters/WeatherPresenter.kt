package com.kennyc.dashweather.presenters

import com.google.android.apps.dashclock.api.DashClockExtension
import com.kennyc.dashweather.SettingsActivity
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.Logger
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data.contract.WeatherContract
import com.kennyc.dashweather.data.model.LocalPreferences
import com.kennyc.dashweather.data.model.WeatherLocation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

private const val TAG = "WeatherPresenter"

class WeatherPresenter @Inject constructor(private val weatherRepo: WeatherRepository,
                                           private val locationRepository: LocationRepository,
                                           private val preferences: LocalPreferences,
                                           private val logger: Logger) : WeatherContract.Presenter {

    private val scope = MainScope()

    private var view: WeatherContract.View? = null

    override fun requestUpdate(reason: Int) {
        if (reason == DashClockExtension.UPDATE_REASON_MANUAL || requireView().canUpdate()) {
            val usesImperial = preferences.getBoolean(SettingsActivity.KEY_USE_IMPERIAL, true)

            if (requireView().hasRequiredPermissions()) {
                scope.launch(Dispatchers.IO) {
                    val location = locationRepository.getLastKnownLocation()
                    if (location != null) {
                        receivedLocation(location, usesImperial)
                    } else {
                        scope.launch(Dispatchers.Main) {
                            locationRepository.requestLocationUpdates()
                                    .catch { logger.e(TAG, "Error fetching location", it) }
                                    .collect { receivedLocation(it, usesImperial) }
                        }

                    }
                }
            } else {
                requireView().onPermissionsRequired()
            }
        }
    }

    private suspend fun receivedLocation(location: WeatherLocation, usesImperial: Boolean) {
        try {
            val weather = weatherRepo.getWeather(location.latitude, location.longitude, usesImperial)
            preferences.saveString(SettingsActivity.KEY_LAST_KNOWN_LATITUDE, weather.latitude.toString())
            preferences.saveString(SettingsActivity.KEY_LAST__KNOWN_LONGITUDE, weather.longitude.toString())
            withContext(Dispatchers.Main) { requireView().onWeatherReceived(weather, usesImperial) }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { requireView().onWeatherFailure(e) }
        }
    }

    override fun setView(view: WeatherContract.View) {
        this.view = view
    }

    override fun requireView(): WeatherContract.View = requireNotNull(view)

    override fun onDestroy() {
        scope.cancel("onDestroy", null)
        view = null
    }
}