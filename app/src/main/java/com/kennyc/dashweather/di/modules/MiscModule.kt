package com.kennyc.dashweather.di.modules

import com.kennyc.dashweather.AndroidLogger
import com.kennyc.dashweather.data.Logger
import dagger.Binds
import dagger.Module

@Module
abstract class MiscModule {

    @Binds
    abstract fun bindsLogger(logger: AndroidLogger): Logger

}