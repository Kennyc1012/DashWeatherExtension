package com.kennyc.dashweather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat

/**
 * Created by kcampagna on 10/6/17.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        const val UPDATE_FREQUENCY_NO_LIMIT = "0"
        const val UPDATE_FREQUENCY_1_HOUR = "1"
        const val UPDATE_FREQUENCY_3_HOURS = "2"
        const val UPDATE_FREQUENCY_4_HOURS = "3"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(activity)
        val frequencyKey = getString(R.string.pref_key_update_frequency)
        val listPreference: ListPreference = findPreference(frequencyKey) as ListPreference
        listPreference.summary = listPreference.entries[preferenceManager.getString(frequencyKey, UPDATE_FREQUENCY_1_HOUR).toInt()]

        listPreference.setOnPreferenceChangeListener { preference, newValue ->
            val listPreference: ListPreference = preference as ListPreference
            listPreference.summary = listPreference.entries[newValue.toString().toInt()]
            true
        }


        val coarsePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val finePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasPermission = coarsePermission || finePermission

        if (!hasPermission) {
            findPreference(getString(R.string.pref_key_permission)).setOnPreferenceClickListener {
                ActivityCompat.requestPermissions(activity,
                        arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        SettingsActivity.PERMISSION_REQUEST_CODE)

                true
            }
        }

        onPermissionUpdated(hasPermission)
    }

    fun onPermissionUpdated(available: Boolean) {
        val stringRes = if (available) R.string.pref_permission_granted else R.string.pref_permission_declined
        findPreference(getString(R.string.pref_key_permission)).setSummary(stringRes)
    }
}