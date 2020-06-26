package com.kennyc.dashweather.api_owm.di

import com.kennyc.dashweather.api_owm.OWMMapApi
import com.kennyc.dashweather.api_owm.interceptor.AppIdInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class OWMModule {

    @Provides
    @Singleton
    fun providesOkHttp(@Named("appId") appId: String): OkHttpClient =
            OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(AppIdInterceptor(appId))
                    .build()

    @Provides
    @Singleton
    fun providesRetrofit(okHttpClient: OkHttpClient): OWMMapApi {
        return Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .run {
                    create(OWMMapApi::class.java)
                }
    }
}