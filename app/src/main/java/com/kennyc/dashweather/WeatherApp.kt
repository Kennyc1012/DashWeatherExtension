package com.kennyc.dashweather

import android.app.Application
import android.os.StrictMode
import com.kennyc.dashweather.di.DaggerAppComponent

class WeatherApp : Application() {

    val component by lazy {
        DaggerAppComponent.builder()
                .appContext(applicationContext)
                .isDebug(BuildConfig.DEBUG)
                .build()
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }
    }
}