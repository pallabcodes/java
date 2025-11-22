package com.example.ledgerpay.di

import com.example.ledgerpay.core.network.ApiClient
import com.example.ledgerpay.core.network.PaymentsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideApiClient(secureStorage: com.example.ledgerpay.core.data.prefs.SecureStorage): ApiClient {
        return ApiClient(secureStorage)
    }

    @Provides
    @Singleton
    fun provideRetrofit(apiClient: ApiClient): Retrofit {
        // TODO: Get base URL from configuration
        return apiClient.retrofit("https://api.example.com/")
    }

    @Provides
    @Singleton
    fun providePaymentsApi(retrofit: Retrofit): PaymentsApi {
        return retrofit.create(PaymentsApi::class.java)
    }
}
