package com.kennyc.dashweather.data.contract

import android.content.SharedPreferences
import com.kennyc.dashweather.data.model.Weather


class WeatherContract {

    interface View {
        fun canUpdate(sharedPreferences: SharedPreferences): Boolean

        fun hasRequiredPermissions(): Boolean

        fun onPermissionsRequired()

        fun onWeatherReceived(weather: Weather)

        fun onWeatherFailure(error: Throwable)
    }

    interface Presenter {

        fun setView(view: View)

        fun requireView(): View

        /**
         * Called when the extension is requesting an update
         *
         * @param reason The reason the update was called
         */
        fun requestUpdate(reason: Int)

        fun onDestroy()
    }
}