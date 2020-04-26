package com.kennyc.dashweather.data.model

interface LocalPreferences {

    fun saveString(key: String, value: String)

    fun getString(key: String, defaultValue: String?): String?

    fun saveDouble(key: String, value: Double)

    fun getDouble(key: String, defaultValue: Double?): Double?

    fun saveBoolean(key: String, value: Boolean)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun saveLong(key: String, value: Long)

    fun getLong(key: String, defaultValue: Long): Long

    fun saveStringSet(key: String, value: Set<String>)

    fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>?
}