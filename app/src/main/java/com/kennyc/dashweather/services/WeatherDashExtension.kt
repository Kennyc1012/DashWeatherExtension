package com.kennyc.dashweather.services

import android.content.*
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.apps.dashclock.api.DashClockExtension
import com.kennyc.dashweather.SettingsActivity
import com.kennyc.dashweather.WeatherApp
import com.kennyc.dashweather.data.contract.WeatherContract
import com.kennyc.dashweather.data.model.Weather
import com.kennyc.dashweather.presenters.WeatherPresenter
import javax.inject.Inject


/**
 * Created by Kenny-PC on 9/22/2017.
 */
class WeatherDashExtension : DashClockExtension(), WeatherContract.View {
    companion object {
        const val KEY_LAST_UPDATED = "com.kennyc.dashweather.LAST_UPDATE"
        const val TAG = "DarkSkyDashExtension"
        const val INTENT_ACTION = "com.kennyc.dashweather.REFRESH"
        const val EXTRA_LATITUDE = "com.kennyc.dashweather.EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "com.kennyc.dashweather.EXTRA_LONGITUDE"

        fun sendBroadcast(context: Context) {
            context.sendBroadcast(Intent(INTENT_ACTION))
        }
    }

    private var broadcastReceiver: BroadcastReceiver? = null

    @Inject
    lateinit var presenter: WeatherPresenter

    override fun onInitialize(isReconnect: Boolean) {
        Log.v(TAG, "onInitialize")
        super.onInitialize(isReconnect)
        (applicationContext as WeatherApp).component.inject(this)

        broadcastReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to unregister receiver", ex)
            }
        }

        broadcastReceiver = DashClockReceiver()
        registerReceiver(broadcastReceiver, IntentFilter(INTENT_ACTION))
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        presenter.onDestroy()
        broadcastReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to unregister receiver:", ex)
            }
        }
        super.onDestroy()
    }

    override fun onUpdateData(reason: Int) {
        Log.v(TAG, "onUpdateData - reason: $reason")
        presenter.requestUpdate(reason)
    }

    override fun canUpdate(sharedPreferences: SharedPreferences): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasRequiredPermissions(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onPermissionsRequired() {
        TODO("Not yet implemented")
    }

    override fun onWeatherReceived(weather: Weather) {
        TODO("Not yet implemented")
    }

    override fun onWeatherFailure(error: Throwable) {
        TODO("Not yet implemented")
    }

    /*  override fun displayWeatherResult(weatherResult: WeatherResult?, usesImperial: Boolean) {
          val current = weatherResult?.currently
          val daily = weatherResult?.daily
          val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
          val uiPreferences = sharedPreferences.getStringSet(getString(R.string.pref_key_details),
                  setOf(SettingsFragment.WEATHER_DETAILS_HIGH_LOW, SettingsFragment.WEATHER_DETAILS_LOCATION))
          val currentTemp: String
          val iconDrawable: Int
          val currentCondition: String

          if (current != null) {
              val temp = Math.round(current.temperature)
              currentTemp = getString(if (usesImperial) R.string.temp_F else R.string.temp_C, temp)
              iconDrawable = current.getIconDrawable()
              currentCondition = current.summary
          } else {
              currentTemp = "???"
              iconDrawable = R.drawable.ic_weather_sunny_black_24dp
              currentCondition = "???"
          }

          val expandedBody = StringBuilder()
          val highLow = presenter.getHighLow(applicationContext, uiPreferences, daily, sharedPreferences.getBoolean(getString(R.string.pref_key_invert_high_low), false), usesImperial)
          highLow?.let { expandedBody.append(it) }

          val humidity = presenter.getHumidity(applicationContext, uiPreferences, current)
          humidity?.let {
              if (expandedBody.isNotEmpty()) expandedBody.append("\n")
              expandedBody.append(humidity)
          }

          val uvIndex = presenter.getUVIndex(applicationContext, uiPreferences, current)
          uvIndex?.let {
              if (expandedBody.isNotEmpty()) expandedBody.append("\n")
              expandedBody.append(uvIndex)
          }

          // TODO Better check this
          val location = presenter.getLocation(applicationContext, uiPreferences, weatherResult!!.latitude, weatherResult!!.longitude)
          location?.let {
              if (expandedBody.isNotEmpty()) expandedBody.append("\n")
              expandedBody.append(location)
          }

          val body = expandedBody.toString()

          publishUpdate(ExtensionData()
                  .visible(true)
                  .clickIntent(Intent(INTENT_ACTION))
                  .icon(iconDrawable)
                  .status(currentTemp)
                  .expandedTitle(currentTemp + " - " + currentCondition)
                  .expandedBody(body))
      }

      override fun onLocationNotFound(exception: Exception?) {
          Log.e(TAG, "unable to get location", exception)
          val lastLocation = getLastSavedLocation()

          if (lastLocation != null) {
              Log.v(TAG, "Using previously saved location " + lastLocation)
              presenter.onLocationReceived(applicationContext, lastLocation[0], lastLocation[1])
          } else {
              publishUpdate(ExtensionData()
                      .visible(true)
                      .icon(R.drawable.ic_map_marker_off_black_24dp)
                      .status(getString(R.string.error_no_location))
                      .expandedTitle(getString(R.string.error_no_location)))
          }

          startService(Intent(applicationContext, LocationService::class.java))
      }

      override fun onPermissionMissing() {
          publishUpdate(ExtensionData()
                  .visible(true)
                  .icon(R.drawable.ic_map_marker_off_black_24dp)
                  .status(getString(R.string.permission_title))
                  .expandedTitle(getString(R.string.permission_title))
                  .expandedBody(getString(R.string.permission_body))
                  .clickIntent(SettingsActivity.createIntent(applicationContext, true)))
      }*/

    /**
     * Returns a 2 length array containing the last latitude[0] and longitude[1] saved into the SharedPreferences. Mull will be returned
     * if no location has been saved
     */
    private fun getLastSavedLocation(): Array<Double>? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val latitude = sharedPreferences.getString(SettingsActivity.KEY_LAST_KNOWN_LATITUDE, null)
        val longitude = sharedPreferences.getString(SettingsActivity.KEY_LAST__KNOWN_LONGITUDE, null)

        if (latitude != null && longitude != null) {
            return arrayOf(latitude.toDouble(), longitude.toDouble())
        }

        return null
    }

    inner class DashClockReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.hasExtra(EXTRA_LATITUDE) && intent.hasExtra(EXTRA_LATITUDE)) {
                val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
                val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
              //  presenter.onLocationReceived(applicationContext, latitude, longitude)
            } else {
                onUpdateData(DashClockExtension.UPDATE_REASON_MANUAL)
            }
        }
    }
}