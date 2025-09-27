package com.example.ledgerpay.di

import com.example.ledgerpay.core.network.ApiClient
import com.example.ledgerpay.core.network.PaymentsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun providePaymentsApi(): PaymentsApi =
        ApiClient.retrofit("https://example.org/").create(PaymentsApi::class.java)
}
