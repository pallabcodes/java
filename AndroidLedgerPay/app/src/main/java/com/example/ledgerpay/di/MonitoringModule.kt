package com.example.ledgerpay.di

import com.example.ledgerpay.core.data.monitoring.Monitoring
import com.example.ledgerpay.monitoring.AppMonitoring
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MonitoringModule {

    @Binds
    @Singleton
    abstract fun bindMonitoring(
        appMonitoring: AppMonitoring
    ): Monitoring
}
