package com.example.ledgerpay.di

import com.example.ledgerpay.core.data.PaymentsRepository
import com.example.ledgerpay.core.data.db.PaymentIntentDao
import com.example.ledgerpay.core.network.PaymentsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {
    @Provides
    @Singleton
    fun providePaymentsRepository(dao: PaymentIntentDao, api: PaymentsApi): PaymentsRepository = PaymentsRepository(dao, api)
}
