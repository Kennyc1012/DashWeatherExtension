package com.kennyc.dashweather.api_owm.di

import com.kennyc.dashweather.api_owm.OWMMapApi
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [OWMModule::class])
interface OWMComponent {

    fun api(): OWMMapApi

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun appId(@Named("appId") appId: String): Builder

        fun build(): OWMComponent
    }

    companion object {
        fun builder(): Builder = DaggerOWMComponent.builder()
    }
}