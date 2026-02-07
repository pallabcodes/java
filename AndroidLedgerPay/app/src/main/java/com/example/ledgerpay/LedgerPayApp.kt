package com.example.ledgerpay

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.ledgerpay.core.data.telemetry.Monitoring
import com.example.ledgerpay.security.DeviceAttestationService
import com.example.ledgerpay.security.DeviceIntegrityRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class LedgerPayApp : Application(), Configuration.Provider {

    @Inject
    lateinit var monitoring: Monitoring

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var deviceAttestationService: DeviceAttestationService

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Initialize Logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Log app startup using monitoring abstraction
        monitoring.log("LedgerPay Application started")

        DeviceIntegrityRefreshWorker.schedule(this)

        appScope.launch {
            val token = deviceAttestationService.ensureValidToken()
            if (!token.isNullOrBlank()) {
                monitoring.log("Device integrity token available")
            } else {
                monitoring.log("Device integrity token unavailable")
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
