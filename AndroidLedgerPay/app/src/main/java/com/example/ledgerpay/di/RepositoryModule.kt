package com.example.ledgerpay.di

import com.example.ledgerpay.core.data.PaymentsRepository
import com.example.ledgerpay.core.data.db.PaymentIntentDao
import com.example.ledgerpay.core.data.telemetry.Monitoring
import com.example.ledgerpay.core.network.PaymentsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePaymentsRepository(
        dao: PaymentIntentDao,
        api: PaymentsApi,
        monitoring: Monitoring,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PaymentsRepository {
        return PaymentsRepository(dao, api, monitoring, ioDispatcher)
    }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher
