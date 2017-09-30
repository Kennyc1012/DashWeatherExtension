package com.kennyc.dashweather.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by Kenny-PC on 9/22/2017.
 */
object ApiClient {
    val darkSkyService: DarkSkyService

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.darksky.net")
                .client(OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        darkSkyService = retrofit.create(DarkSkyService::class.java)
    }
}