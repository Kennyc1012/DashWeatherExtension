package com.kennyc.dashweather

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.kennyc.dashweather.services.WeatherDashExtension

/**
 * Created by kcampagna on 10/6/17.
 */
class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
        const val KEY_PROMPT_PERMISSIONS = "SettingsActivity.PROMPT_PERMISSIONS"
        const val KEY_LAST_KNOWN_LATITUDE = "SettingsActivity.LAST_KNOWN_LATITUDE"
        const val KEY_LAST__KNOWN_LONGITUDE = "SettingsActivity.LAST_KNOWN_LONGITUDE"
        const val KEY_UPDATE_FREQUENCY = "SettingsActivity.UPDATE_FREQUENCY"
        const val KEY_SHOW_WEATHER_DETAILS = "SettingsActivity.SHOW_WEATHER_DETAILS"
        const val KEY_INVERT_HIGH_LOW = "SettingsActivity.INVERT_HIGH_LOW"

        fun createIntent(context: Context, promptPermissions: Boolean): Intent {
            return Intent(context, SettingsActivity::class.java).putExtra(KEY_PROMPT_PERMISSIONS, promptPermissions)
        }
    }

    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsFragment = SettingsFragment()

        supportFragmentManager.beginTransaction()
                .add(android.R.id.content, settingsFragment)
                .commit()

        val extras = intent

        if (extras != null && extras.getBooleanExtra(KEY_PROMPT_PERMISSIONS, false)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                WeatherDashExtension.sendBroadcast(applicationContext)
                val hasPermission = grantResults.contains(PackageManager.PERMISSION_GRANTED)
                settingsFragment?.onPermissionUpdated(hasPermission)
            }
        }
    }
}