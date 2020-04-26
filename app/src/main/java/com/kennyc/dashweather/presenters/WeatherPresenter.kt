package com.kennyc.dashweather.presenters

import android.content.SharedPreferences
import com.google.android.apps.dashclock.api.DashClockExtension
import com.kennyc.dashweather.SettingsActivity
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data.contract.WeatherContract
import com.kennyc.dashweather.data.model.WeatherLocation
import com.kennyc.dashweather.data.model.exception.LocationNotFoundException
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WeatherPresenter @Inject constructor(private val weatherRepo: WeatherRepository,
                                           private val locationRepository: LocationRepository,
        /*TODO Abstract this away from SharedPreferences*/        private val sharedPreferences: SharedPreferences) : WeatherContract.Presenter {

    private val disposables = CompositeDisposable()

    private var view: WeatherContract.View? = null

    override fun requestUpdate(reason: Int) {
        if (reason == DashClockExtension.UPDATE_REASON_MANUAL || requireView().canUpdate()) {
            if (requireView().hasRequiredPermissions()) {
                // Get last known location
                disposables.add(locationRepository.getLastKnownLocation()
                        .subscribeOn(Schedulers.io())
                        .doOnError { error ->
                            when (error) {
                                is LocationNotFoundException -> {
                                    // If we do not have a last known location, request updates
                                    locationRepository.requestLocationUpdates()
                                            .doOnError {
                                                // If no location was found, look for a last known one that was saved to local preferences
                                                val lastLat = sharedPreferences.getString(SettingsActivity.KEY_LAST_KNOWN_LATITUDE, null)
                                                val lastLon = sharedPreferences.getString(SettingsActivity.KEY_LAST_KNOWN_LATITUDE, null)

                                                if (!lastLat.isNullOrEmpty() && !lastLon.isNullOrEmpty()) {
                                                    Observable.just(WeatherLocation(lastLat.toDouble(), lastLon.toDouble()))
                                                } else {
                                                    Observable.error(it)
                                                }
                                            }
                                }
                                else -> Observable.error(error)
                            }
                        }
                        // Once location is obtained, get weather info
                        .flatMap { weatherRepo.getWeather(it.latitude, it.longitude) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ weather ->
                            // Save last received weather location to local preferences
                            sharedPreferences.edit()
                                    .putString(SettingsActivity.KEY_LAST_KNOWN_LATITUDE, weather.latitude.toString())
                                    .putString(SettingsActivity.KEY_LAST__KNOWN_LONGITUDE, weather.longitude.toString())
                                    .apply()

                            requireView().onWeatherReceived(weather)
                        }, { error ->
                            requireView().onWeatherFailure(error)
                        }))
            } else {
                requireView().onPermissionsRequired()
            }
        }
    }

    override fun setView(view: WeatherContract.View) {
        this.view = view
    }

    override fun requireView(): WeatherContract.View = requireNotNull(view)

    override fun onDestroy() {
        disposables.clear()
        view = null
    }
}