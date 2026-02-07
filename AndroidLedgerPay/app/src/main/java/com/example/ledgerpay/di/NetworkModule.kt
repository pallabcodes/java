package com.example.ledgerpay.di

import android.content.Context
import com.example.ledgerpay.BuildConfig
import com.example.ledgerpay.core.data.prefs.SecureStorage
import com.example.ledgerpay.core.network.ApiClient
import com.example.ledgerpay.core.network.PaymentsApi
import com.example.ledgerpay.security.PinValidation
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val LEDGERPAY_HOST = "api.ledgerpay.com"

    @Provides
    @Singleton
    fun provideApiClient(
        secureStorage: SecureStorage,
        @ApplicationContext context: Context
    ): ApiClient {
        val ledgerPins = PinValidation.validatedPins(
            isDebugBuild = BuildConfig.DEBUG,
            primaryPin = BuildConfig.LEDGERPAY_PIN_PRIMARY,
            backupPin = BuildConfig.LEDGERPAY_PIN_BACKUP
        )

        return ApiClient(
            context = context,
            authTokenProvider = { secureStorage.getAuthToken() },
            deviceIntegrityTokenProvider = { secureStorage.getDeviceIntegrityToken(minValidityMs = 60_000L) },
            additionalPinsByHost = mapOf(LEDGERPAY_HOST to ledgerPins),
            enableVerboseLogging = BuildConfig.DEBUG && BuildConfig.DEBUG_NETWORK_ENABLED
        )
    }

    @Provides
    @Singleton
    fun provideRetrofit(apiClient: ApiClient): Retrofit {
        return apiClient.retrofit(BuildConfig.API_BASE_URL)
    }

    @Provides
    @Singleton
    fun providePaymentsApi(retrofit: Retrofit): PaymentsApi {
        return retrofit.create(PaymentsApi::class.java)
    }
}
