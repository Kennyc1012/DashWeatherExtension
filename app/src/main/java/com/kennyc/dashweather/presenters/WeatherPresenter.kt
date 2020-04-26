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
import javax.inject.Inject

class WeatherPresenter @Inject constructor(private val weatherRepo: WeatherRepository,
                                           private val locationRepository: LocationRepository,
                                           private val sharedPreferences: SharedPreferences) : WeatherContract.Presenter {

    private val disposables = CompositeDisposable()

    private var view: WeatherContract.View? = null

    override fun requestUpdate(reason: Int) {
        if (reason == DashClockExtension.UPDATE_REASON_MANUAL || requireView().canUpdate(sharedPreferences)) {
            if (requireView().hasRequiredPermissions()) {
                disposables.add(locationRepository.getLastKnownLocation()
                        .subscribeOn(Schedulers.io())
                        .flatMap { weatherRepo.getWeather(it.latitude, it.longitude) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ weather ->
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
    }
}