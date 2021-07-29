package com.kennyc.dashweather.presenters

import com.google.android.apps.dashclock.api.DashClockExtension
import com.kennyc.dashweather.SettingsActivity
import com.kennyc.dashweather.data.LocationRepository
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data.contract.WeatherContract
import com.kennyc.dashweather.data.model.LocalPreferences
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

private const val TAG = "WeatherPresenter"

class WeatherPresenter @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val preferences: LocalPreferences,
) : WeatherContract.Presenter {

    private val disposables = CompositeDisposable()

    private var view: WeatherContract.View? = null

    override fun requestUpdate(reason: Int) {
        if (reason == DashClockExtension.UPDATE_REASON_MANUAL || requireView().canUpdate()) {
            val usesImperial = preferences.getBoolean(SettingsActivity.KEY_USE_IMPERIAL, true)

            if (requireView().hasRequiredPermissions()) {
                val disposable = locationRepository.getLastKnownLocation()
                    .switchIfEmpty(locationRepository.requestLocationUpdates().singleOrError())
                    .flatMap {
                        preferences.saveString(
                            SettingsActivity.KEY_LAST_KNOWN_LATITUDE,
                            it.latitude.toString()
                        )

                        preferences.saveString(
                            SettingsActivity.KEY_LAST__KNOWN_LONGITUDE,
                            it.longitude.toString()
                        )

                        weatherRepo.getWeather(it.latitude, it.longitude, usesImperial)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result -> requireView().onWeatherReceived(result, usesImperial) }

                disposables.add(disposable)
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