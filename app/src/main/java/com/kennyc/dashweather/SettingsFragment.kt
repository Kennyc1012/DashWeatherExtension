package com.kennyc.dashweather

import android.os.Bundle
import android.preference.PreferenceManager
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
        val frequencyKey = getString(R.string.pref_key_update_frequency)
        val listPreference: ListPreference = findPreference(frequencyKey) as ListPreference
        listPreference.summary = listPreference.entries[PreferenceManager.getDefaultSharedPreferences(activity)
                .getString(frequencyKey, UPDATE_FREQUENCY_1_HOUR).toInt()]

        listPreference.setOnPreferenceChangeListener { preference, newValue ->
            val listPreference: ListPreference = preference as ListPreference
            listPreference.summary = listPreference.entries[newValue.toString().toInt()]
            true
        }
    }

}