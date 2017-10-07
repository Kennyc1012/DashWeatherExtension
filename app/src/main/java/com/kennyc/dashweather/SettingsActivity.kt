package com.kennyc.dashweather

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.kennyc.dashweather.services.DarkSkyDashExtension

/**
 * Created by kcampagna on 10/6/17.
 */
class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
        const val KEY_PROMPT_PERMISSIONS = "SettingsActivity.PROMPT_PERMISSIONS"

        fun createIntent(context: Context, promptPermissions: Boolean): Intent {
            return Intent(context, SettingsActivity::class.java).putExtra(KEY_PROMPT_PERMISSIONS, promptPermissions)
        }
    }

    private var settingsFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsFragment = SettingsFragment()

        supportFragmentManager.
                beginTransaction()
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
                DarkSkyDashExtension.sendBroadcast(applicationContext)
                val hasPermission = grantResults.contains(PackageManager.PERMISSION_GRANTED)
                settingsFragment?.onPermissionUpdated(hasPermission)
            }
        }
    }
}