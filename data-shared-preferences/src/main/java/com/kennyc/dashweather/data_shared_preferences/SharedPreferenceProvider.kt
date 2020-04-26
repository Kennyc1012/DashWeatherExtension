package com.kennyc.dashweather.data_shared_preferences

import android.content.SharedPreferences
import com.kennyc.dashweather.data.model.LocalPreferences

class SharedPreferenceProvider(private val preferences: SharedPreferences) : LocalPreferences {

    override fun saveString(key: String, value: String) = preferences.edit().putString(key, value).apply()

    override fun getString(key: String, defaultValue: String?): String? = preferences.getString(key, defaultValue)

    override fun saveDouble(key: String, value: Double) = preferences.edit().putString(key, value.toString()).apply()

    override fun getDouble(key: String, defaultValue: Double?): Double? {
        return when (val doubleString = preferences.getString(key, null)) {
            null -> defaultValue
            else -> {
                try {
                    doubleString.toDouble()
                } catch (e: Exception) {
                    defaultValue
                }
            }
        }
    }

    override fun saveBoolean(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).apply()

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = preferences.getBoolean(key, defaultValue)

    override fun saveLong(key: String, value: Long) = preferences.edit().putLong(key, value).apply()

    override fun getLong(key: String, defaultValue: Long): Long = preferences.getLong(key, defaultValue)

    override fun saveStringSet(key: String, value: Set<String>) = preferences.edit().putStringSet(key, value).apply()

    override fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? = preferences.getStringSet(key, defaultValue) as Set<String>
}