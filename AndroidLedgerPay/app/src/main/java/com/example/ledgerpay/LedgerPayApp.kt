package com.example.ledgerpay

import android.app.Application
import com.example.ledgerpay.core.data.monitoring.Monitoring
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class LedgerPayApp : Application() {

    @Inject
    lateinit var monitoring: Monitoring

    override fun onCreate() {
        super.onCreate()

        // Initialize monitoring/logging system
        monitoring

        // Log app startup
        Timber.i("LedgerPay Application started")
    }
}
