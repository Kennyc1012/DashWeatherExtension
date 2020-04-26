package com.kennyc.dashweather.presenters

import android.content.SharedPreferences
import com.google.android.apps.dashclock.api.DashClockExtension
import com.kennyc.dashweather.SettingsActivity
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data.contract.WeatherContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class WeatherPresenter(private val weatherRepo: WeatherRepository,
                       private val locationRepository: LocationRepository,
                       private val sharedPreferences: SharedPreferences,
                       private val view: WeatherContract.View) : WeatherContract.Presenter {

    private val disposables = CompositeDisposable()

    override fun requestUpdate(reason: Int) {
        if (reason == DashClockExtension.UPDATE_REASON_MANUAL || view.canUpdate(sharedPreferences)) {
            if (view.hasRequiredPermissions()) {
                disposables.add(locationRepository.getLastKnownLocation()
                        .subscribeOn(Schedulers.io())
                        .flatMap { weatherRepo.getWeather(it.latitude, it.longitude) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ weather ->
                            sharedPreferences.edit()
                                    .putString(SettingsActivity.KEY_LAST_KNOWN_LATITUDE, weather.latitude.toString())
                                    .putString(SettingsActivity.KEY_LAST__KNOWN_LONGITUDE, weather.longitude.toString())
                                    .apply()

                            view.onWeatherReceived(weather)
                        }, { error ->
                            view.onWeatherFailure(error)
                        }))
            } else {
                view.onPermissionsRequired()
            }
        }
    }

    override fun onDestroy() {
        disposables.clear()
    }
}