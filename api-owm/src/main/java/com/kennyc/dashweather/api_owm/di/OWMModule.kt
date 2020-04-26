package com.kennyc.dashweather.api_owm.di

import com.kennyc.dashweather.api_owm.OWMMapApi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class OWMModule {

    @Provides
    @Singleton
    fun providesOkHttp(): OkHttpClient =
            OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build()

    @Provides
    @Singleton
    fun providesRetrofit(okHttpClient: OkHttpClient): OWMMapApi {
        return Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .run {
                    create(OWMMapApi::class.java)
                }
    }
}