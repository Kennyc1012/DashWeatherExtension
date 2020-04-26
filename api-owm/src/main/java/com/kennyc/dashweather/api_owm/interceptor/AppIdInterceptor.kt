package com.kennyc.dashweather.api_owm.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AppIdInterceptor(private val appId: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = chain.request().url.newBuilder().addQueryParameter("appId",appId).build()

        return request.newBuilder().url(url).build().run {
            chain.proceed(this)
        }
    }
}