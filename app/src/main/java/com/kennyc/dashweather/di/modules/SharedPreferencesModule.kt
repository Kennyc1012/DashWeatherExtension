package com.kennyc.dashweather.di.modules

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPreferencesModule {

    @Provides
    @Singleton
    fun providesSharedPreferences(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

}