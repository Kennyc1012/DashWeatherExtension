package com.kennyc.dashweather.data.contract

import com.kennyc.dashweather.data.model.Weather


class WeatherContract {

    interface View {
        fun canUpdate(): Boolean

        fun hasRequiredPermissions(): Boolean

        fun onPermissionsRequired()

        fun onWeatherReceived(weather: Weather, usesImperial: Boolean)

        fun onWeatherFailure(error: Throwable)
    }

    interface Presenter {

        fun setView(view: View)

        fun requireView(): View

        fun requestUpdate(reason: Int)

        fun onDestroy()
    }
}