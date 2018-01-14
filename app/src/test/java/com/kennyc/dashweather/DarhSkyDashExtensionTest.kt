package com.kennyc.dashweather

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.apps.dashclock.api.DashClockExtension
import com.kennyc.dashweather.contracts.DarkSkyContract
import com.kennyc.dashweather.models.DailyWeatherModel
import com.kennyc.dashweather.models.WeatherModel
import com.kennyc.dashweather.presenters.DarkSkyPresenter
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(org.mockito.junit.MockitoJUnitRunner::class)
class DarhSkyDashExtensionTest {

    val context: Context = Mockito.mock(Context::class.java, Mockito.RETURNS_DEEP_STUBS)
    val view: DarkSkyContract.View = Mockito.mock(DarkSkyContract.View::class.java)
    val presenter: DarkSkyPresenter = DarkSkyPresenter(view)

    @Test
    fun testNotNull() {
        Assert.assertNotNull(view)
        Assert.assertNotNull(presenter)
        Assert.assertNotNull(context)
    }

    @Test
    fun testPermissionNotAvailable() {
        Mockito.`when`(context.checkPermission(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(PackageManager.PERMISSION_DENIED)
        presenter.onRequestUpdate(context, DashClockExtension.UPDATE_REASON_MANUAL)
        Mockito.verify(view).onPermissionMissing()
    }

    @Test
    fun testHumidity() {
        var settings = emptySet<String>()
        val model = WeatherModel()
        // Should return null since not in settings
        model.humidity = .5f
        Assert.assertNull(presenter.getHumidity(context, settings, model))

        // Should return the humidity with a percentage sign
        settings = setOf(SettingsFragment.WEATHER_DETAILS_HUMIDITY)
        Mockito.`when`(context.getString(Mockito.anyInt(), Mockito.any())).thenReturn(Math.round(model.humidity * 100).toString())
        Assert.assertEquals("50%", presenter.getHumidity(context, settings, model))

        // If passed a null WeatherModel, "???" will be returned
        Assert.assertEquals("???", presenter.getHumidity(context, settings, null))
    }

    @Test
    fun testUVIndex() {
        var settings = emptySet<String>()
        val model = WeatherModel()
        // Should return null since not in settings
        model.uvIndex = 3
        Assert.assertNull(presenter.getUVIndex(context, settings, model))

        // Should return the exact uv index number
        settings = setOf(SettingsFragment.WEATHER_DETAILS_UV_INDEX)
        Mockito.`when`(context.getString(Mockito.anyInt(), Mockito.any())).thenReturn(model.uvIndex.toString())
        Assert.assertEquals("3", presenter.getUVIndex(context, settings, model))

        // If passed a null WeatherModel, "???" will be returned
        Assert.assertEquals("???", presenter.getUVIndex(context, settings, null))
    }

    @Test
    fun testHighLow() {
        var settings = emptySet<String>()
        var invert = false
        val daily = DailyWeatherModel()
        val model = WeatherModel()
        model.temperatureHigh = 50.0
        model.temperatureLow = 40.0
        daily.data = listOf(model)
        Assert.assertNull(presenter.getHighLow(context, settings, daily, invert, true))

        val stringResource = if (invert) R.string.high_low_invert else R.string.high_low
        settings = setOf(SettingsFragment.WEATHER_DETAILS_HIGH_LOW)
        Mockito.`when`(context.getString(Mockito.anyInt(), Mockito.any())).thenReturn("High: " + model.temperatureHigh + "F, Low: " + model.temperatureLow + "F")
        Assert.assertEquals("High: 50.0F, Low: 40.0F", presenter.getHighLow(context, settings, daily, invert, true))
    }
}