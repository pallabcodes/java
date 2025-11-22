package com.example.ledgerpay.di

import android.app.Application
import androidx.room.Room
import com.example.ledgerpay.core.data.db.AppDatabase
import com.example.ledgerpay.core.data.db.PaymentIntentDao
import com.example.ledgerpay.core.data.prefs.SecureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDb(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "ledgerpay.db")
            .fallbackToDestructiveMigration() // TODO: Implement proper migrations
            .build()

    @Provides
    fun providePaymentIntentDao(db: AppDatabase): PaymentIntentDao = db.paymentIntentDao()

    @Provides
    @Singleton
    fun provideSecureStorage(app: Application): SecureStorage {
        return SecureStorage(app)
    }
}
