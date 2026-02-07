package com.example.ledgerpay

import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.ledgerpay.core.network.NetworkOptimizer
import com.example.ledgerpay.core.data.offline.SyncService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkOptimizer: NetworkOptimizer

    @Inject
    lateinit var syncService: SyncService

    private val performanceMonitor = PerformanceMonitor()
    private var appStartTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val createTime = measureTimeMillis {
            appStartTime = System.currentTimeMillis()
            super.onCreate(savedInstanceState)

            // Performance: Enable StrictMode in debug builds for performance monitoring
            if (BuildConfig.DEBUG && BuildConfig.PERFORMANCE_MONITORING_ENABLED) {
                enableStrictMode()
            }

            // Performance: Pre-warm critical components
            preWarmComponents()

            // Performance: Initialize UI with optimized rendering
            setContent {
                com.example.ledgerpay.navigation.ui.AppScaffold()
            }

            // Performance: Start background sync if needed
            scheduleBackgroundTasks()
        }

        Log.d("MainActivity", "onCreate completed in ${createTime}ms")

        // Performance: Monitor memory usage
        performanceMonitor.startMemoryMonitoring()
    }

    override fun onResume() {
        super.onResume()

        // Performance: Record app resume time
        performanceMonitor.recordAppResume()

        // Network: Check network quality on resume
        val networkQuality = networkOptimizer.getNetworkQuality()
        Log.d("MainActivity", "Network quality on resume: $networkQuality")

        // Performance: Trigger garbage collection if memory pressure is high
        if (networkOptimizer.getMemoryPressure() == NetworkOptimizer.MemoryPressure.HIGH) {
            System.gc()
        }
    }

    override fun onPause() {
        super.onPause()

        // Performance: Record app pause time and cleanup
        performanceMonitor.recordAppPause()

        // Performance: Trim memory if needed
        onTrimMemory(TRIM_MEMORY_UI_HIDDEN)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        when (level) {
            TRIM_MEMORY_COMPLETE,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_BACKGROUND -> {
                // Aggressive cleanup for critical memory pressure
                Log.w("MainActivity", "High memory pressure detected, level: $level")
                performanceMonitor.recordMemoryPressure(level)

                // Clear caches and stop background tasks
                clearNonEssentialCaches()
                stopNonEssentialBackgroundTasks()
            }
            TRIM_MEMORY_UI_HIDDEN -> {
                // App is in background, reduce memory usage
                reduceMemoryUsage()
            }
            TRIM_MEMORY_RUNNING_CRITICAL,
            TRIM_MEMORY_RUNNING_LOW -> {
                // Running low on memory, cleanup aggressively
                Log.w("MainActivity", "Critical memory pressure, level: $level")
                emergencyMemoryCleanup()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()

        Log.w("MainActivity", "System low memory warning")
        performanceMonitor.recordLowMemory()

        // Emergency cleanup
        emergencyMemoryCleanup()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
                .build()
        )
    }

    private fun preWarmComponents() {
        // Pre-warm critical components to improve perceived performance
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Pre-warm network connections
                networkOptimizer.getNetworkQuality()

                // Pre-warm database connections
                // Add any other pre-warming logic here

            } catch (e: Exception) {
                Log.w("MainActivity", "Error during component pre-warming", e)
            }
        }
    }

    private fun scheduleBackgroundTasks() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Check if we need to sync offline transactions
                val syncStats = syncService.getSyncStatistics()
                if (syncStats.pendingSync > 0) {
                    // Delay sync to avoid impacting app startup
                    delay(2000)
                    syncService.syncNow()
                }
            } catch (e: Exception) {
                Log.w("MainActivity", "Error scheduling background tasks", e)
            }
        }
    }

    private fun clearNonEssentialCaches() {
        // Clear image caches, temporary data, etc.
        // Implementation depends on your caching strategy
        Log.d("MainActivity", "Clearing non-essential caches")
    }

    private fun stopNonEssentialBackgroundTasks() {
        // Stop background sync, image loading, etc.
        Log.d("MainActivity", "Stopping non-essential background tasks")
        syncService.cancelSync()
    }

    private fun reduceMemoryUsage() {
        // Reduce memory usage when app goes to background
        Log.d("MainActivity", "Reducing memory usage")

        // Clear view caches
        // Reduce bitmap cache sizes
        // Cancel pending operations
    }

    private fun emergencyMemoryCleanup() {
        Log.w("MainActivity", "Performing emergency memory cleanup")

        // Most aggressive cleanup
        clearNonEssentialCaches()
        stopNonEssentialBackgroundTasks()

        // Force garbage collection
        System.gc()
        System.runFinalization()
        System.gc()

        // Log memory stats
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        Log.w("MainActivity", "Memory after cleanup - Used: ${usedMemory / 1024 / 1024}MB, Max: ${maxMemory / 1024 / 1024}MB")
    }

    override fun onDestroy() {
        super.onDestroy()

        // Performance: Cleanup resources
        performanceMonitor.stopMemoryMonitoring()
        lifecycleScope.cancel()
    }
}

class PerformanceMonitor {

    private var memoryMonitoringJob: Job? = null
    private var appResumeTime: Long = 0
    private var appPauseTime: Long = 0

    fun startMemoryMonitoring() {
        memoryMonitoringJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                recordMemoryUsage()
                delay(30000) // Check every 30 seconds
            }
        }
    }

    fun stopMemoryMonitoring() {
        memoryMonitoringJob?.cancel()
    }

    fun recordAppResume() {
        appResumeTime = System.currentTimeMillis()
    }

    fun recordAppPause() {
        appPauseTime = System.currentTimeMillis()
        val sessionDuration = appPauseTime - appResumeTime
        Log.d("PerformanceMonitor", "App session duration: ${sessionDuration}ms")
    }

    fun recordMemoryPressure(level: Int) {
        Log.w("PerformanceMonitor", "Memory pressure level: $level")
        // Could send to analytics service
    }

    fun recordLowMemory() {
        Log.e("PerformanceMonitor", "Low memory condition detected")
        // Could trigger emergency measures or send alerts
    }

    private fun recordMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usedPercent = (usedMemory.toDouble() / maxMemory * 100).toInt()

        if (usedPercent > 80) {
            Log.w("PerformanceMonitor", "High memory usage: ${usedMemory / 1024 / 1024}MB (${usedPercent}%)")
        } else {
            Log.d("PerformanceMonitor", "Memory usage: ${usedMemory / 1024 / 1024}MB (${usedPercent}%)")
        }
    }
}
