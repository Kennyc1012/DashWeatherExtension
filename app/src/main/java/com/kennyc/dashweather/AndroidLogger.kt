package com.kennyc.dashweather

import android.util.Log
import com.kennyc.dashweather.data.Logger
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AndroidLogger @Inject constructor(@Named("isDebug") private val isDebug: Boolean) : Logger {

    override fun v(tag: String, message: String) {
        if (isDebug) Log.v(tag, message)
    }

    override fun w(tag: String, message: String) {
        if (isDebug) Log.w(tag, message)
    }

    override fun i(tag: String, message: String) {
        if (isDebug) Log.i(tag, message)
    }

    override fun d(tag: String, message: String) {
        if (isDebug) Log.d(tag, message)
    }

    override fun e(tag: String, message: String) {
        if (isDebug) Log.e(tag, message)
    }

    override fun e(tag: String, message: String, error: Throwable?) {
        if (isDebug) Log.e(tag, message, error)
    }
}