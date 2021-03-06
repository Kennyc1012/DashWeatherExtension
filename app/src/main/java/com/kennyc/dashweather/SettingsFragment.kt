package com.kennyc.dashweather

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import com.kennyc.dashweather.data.Logger
import com.kennyc.dashweather.data.WeatherRepository
import com.kennyc.dashweather.data.model.LocalPreferences
import javax.inject.Inject


/**
 * Created by kcampagna on 10/6/17.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        const val UPDATE_FREQUENCY_NO_LIMIT = "0"
        const val UPDATE_FREQUENCY_1_HOUR = "1"
        const val UPDATE_FREQUENCY_3_HOURS = "2"
        const val UPDATE_FREQUENCY_4_HOURS = "3"

        const val WEATHER_DETAILS_HIGH_LOW = "0"
        const val WEATHER_DETAILS_HUMIDITY = "1"
        const val WEATHER_DETAILS_LOCATION = "2"
    }

    @Inject
    lateinit var preferences: LocalPreferences

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var repo: WeatherRepository

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        (context!!.applicationContext as WeatherApp).component.inject(this)
        addPreferencesFromResource(R.xml.settings)
        val frequencyKey = SettingsActivity.KEY_UPDATE_FREQUENCY
        val listPreference: ListPreference = findPreference(frequencyKey) as ListPreference
        listPreference.summary = listPreference.entries[preferences.getString(frequencyKey, UPDATE_FREQUENCY_1_HOUR)!!.toInt()]

        listPreference.setOnPreferenceChangeListener { preference, newValue ->
            val listPreference: ListPreference = preference as ListPreference
            listPreference.summary = listPreference.entries[newValue.toString().toInt()]
            true
        }

        val detailsKey = SettingsActivity.KEY_SHOW_WEATHER_DETAILS
        val weatherDetails: MultiSelectListPreference = findPreference(detailsKey) as MultiSelectListPreference

        preferences.getStringSet(detailsKey,
                setOf(SettingsFragment.WEATHER_DETAILS_HIGH_LOW, SettingsFragment.WEATHER_DETAILS_LOCATION))?.let {
            setWeatherDetails(weatherDetails, it)
        }

        weatherDetails.setOnPreferenceChangeListener { preference, newValue ->
            setWeatherDetails(preference as MultiSelectListPreference, newValue as Set<String>)
            true
        }

        val version = findPreference(getString(R.string.pref_key_version))
        try {
            val act = activity as Activity
            val pInfo = act.packageManager.getPackageInfo(act.packageName, 0)
            version.summary = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            logger.e("Settings", "Unable to get version number")
        }

        checkPermissions()

        findPreference(getString(R.string.pref_key_powered_by)).summary = repo.getWeatherProviderName()
    }

    private fun checkPermissions() {
        val context = activity as Context
        val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasPermission = coarsePermission || finePermission

        if (!hasPermission) {
            findPreference(getString(R.string.pref_key_permission)).setOnPreferenceClickListener {
                ActivityCompat.requestPermissions(context as Activity,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        SettingsActivity.PERMISSION_REQUEST_CODE)

                true
            }
        }

        onPermissionUpdated(hasPermission)
    }

    private fun setWeatherDetails(weatherDetails: MultiSelectListPreference, uiPreferences: Set<String>) {
        val summary = StringBuilder()
        val size = uiPreferences.size

        uiPreferences.withIndex().forEach {
            summary.append(weatherDetails.entries[weatherDetails.findIndexOfValue(it.value)])
            if (it.index < size - 1) summary.append("\n")
        }

        weatherDetails.summary = summary.toString()
    }

    fun onPermissionUpdated(available: Boolean) {
        val stringRes = if (available) R.string.preference_permission_granted else R.string.preference_permission_declined
        findPreference(getString(R.string.pref_key_permission)).setSummary(stringRes)
    }
}