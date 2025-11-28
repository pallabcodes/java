package com.example.ledgerpay.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkOptimizer @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val COMPRESSION_THRESHOLD_BYTES = 1024
        private const val CONNECTION_POOL_SIZE = 10
        private const val KEEP_ALIVE_DURATION_SECONDS = 300L
    }

    // Network quality detection
    enum class NetworkQuality {
        EXCELLENT, // 4G+, WiFi with good signal
        GOOD,      // 3G, WiFi with moderate signal
        POOR,      // 2G, Edge, poor WiFi
        OFFLINE    // No network
    }

    fun getNetworkQuality(): NetworkQuality {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return NetworkQuality.OFFLINE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkQuality.OFFLINE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                // For WiFi, we could check signal strength, but for simplicity assume good
                NetworkQuality.EXCELLENT
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> {
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                            capabilities.linkDownstreamBandwidthKbps > 10000 -> NetworkQuality.EXCELLENT
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                            capabilities.linkDownstreamBandwidthKbps > 5000 -> NetworkQuality.GOOD
                            else -> NetworkQuality.POOR
                        }
                    }
                    else -> NetworkQuality.POOR
                }
            }
            else -> NetworkQuality.OFFLINE
        }
    }

    // Adaptive timeout interceptor
    val adaptiveTimeoutInterceptor = Interceptor { chain ->
        val quality = getNetworkQuality()
        val request = chain.request()

        val timeoutMultiplier = when (quality) {
            NetworkQuality.EXCELLENT -> 1.0
            NetworkQuality.GOOD -> 1.5
            NetworkQuality.POOR -> 2.5
            NetworkQuality.OFFLINE -> throw IOException("No network connection available")
        }

        // Apply adaptive timeouts based on network quality
        val connectTimeout = (10_000L * timeoutMultiplier).toLong().coerceAtMost(30_000L)
        val readTimeout = (15_000L * timeoutMultiplier).toLong().coerceAtMost(45_000L)
        val writeTimeout = (15_000L * timeoutMultiplier).toLong().coerceAtMost(45_000L)

        val adaptedRequest = request.newBuilder()
            .tag(TimeoutConfig(connectTimeout, readTimeout, writeTimeout))
            .build()

        chain.withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .withWriteTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .proceed(adaptedRequest)
    }

    // Request compression interceptor
    val compressionInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalBody = originalRequest.body

        // Compress requests larger than threshold
        val compressedRequest = if (originalBody != null && originalBody.contentLength() > COMPRESSION_THRESHOLD_BYTES) {
            originalRequest.newBuilder()
                .header("Content-Encoding", "gzip")
                .method(originalRequest.method, GzipRequestBody(originalBody))
                .build()
        } else {
            originalRequest
        }

        chain.proceed(compressedRequest)
    }

    // Response caching interceptor
    val cacheControlInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        // Add cache control headers based on response type
        val cacheControl = when {
            request.url.toString().contains("/api/user/profile") -> "private, max-age=300" // 5 minutes
            request.url.toString().contains("/api/transactions/recent") -> "private, max-age=60" // 1 minute
            request.url.toString().contains("/api/balance") -> "private, max-age=30" // 30 seconds
            else -> "no-cache"
        }

        response.newBuilder()
            .header("Cache-Control", cacheControl)
            .build()
    }

    // Connection pooling configuration
    fun getConnectionPoolConfig() = okhttp3.ConnectionPool(
        CONNECTION_POOL_SIZE,
        KEEP_ALIVE_DURATION_SECONDS,
        TimeUnit.SECONDS
    )

    // DNS optimization
    val dnsOptimizer = okhttp3.Dns { hostname ->
        try {
            // Use system DNS resolution with caching
            java.net.InetAddress.getAllByName(hostname).toList()
        } catch (e: Exception) {
            // Fallback to default DNS
            okhttp3.Dns.SYSTEM.lookup(hostname)
        }
    }

    // Bandwidth-aware request prioritization
    fun shouldDeferRequest(request: okhttp3.Request): Boolean {
        val quality = getNetworkQuality()

        // Defer heavy requests on poor connections
        return when (quality) {
            NetworkQuality.POOR -> {
                // Defer image downloads, large file uploads, etc.
                request.url.toString().contains("/images/") ||
                request.url.toString().contains("/files/") ||
                (request.body?.contentLength() ?: 0) > 100_000 // 100KB
            }
            NetworkQuality.OFFLINE -> true
            else -> false
        }
    }

    // Memory-efficient response handling
    val memoryEfficientInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())

        // For large responses, suggest streaming
        val contentLength = response.header("Content-Length")?.toLongOrNull() ?: 0
        if (contentLength > 1_000_000) { // 1MB
            // Add header to indicate large response
            response.newBuilder()
                .header("X-Large-Response", "true")
                .build()
        } else {
            response
        }
    }

    // Battery-aware request scheduling
    fun isBatteryOptimized(): Boolean {
        // Check if device is in battery saver mode
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                powerManager.isPowerSaveMode
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Get optimal thread pool size based on device capabilities
    fun getOptimalThreadPoolSize(): Int {
        val cores = Runtime.getRuntime().availableProcessors()
        val quality = getNetworkQuality()
        val batteryOptimized = isBatteryOptimized()

        val baseSize = when (quality) {
            NetworkQuality.EXCELLENT -> cores * 2
            NetworkQuality.GOOD -> cores
            NetworkQuality.POOR -> maxOf(cores / 2, 1)
            NetworkQuality.OFFLINE -> 1
        }

        return if (batteryOptimized) maxOf(baseSize / 2, 1) else baseSize
    }

    // Memory usage monitoring
    private val memoryStats = mutableListOf<Long>()

    fun recordMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        memoryStats.add(usedMemory)

        // Keep only last 10 measurements
        if (memoryStats.size > 10) {
            memoryStats.removeAt(0)
        }
    }

    fun getAverageMemoryUsage(): Long {
        return if (memoryStats.isNotEmpty()) {
            memoryStats.average().toLong()
        } else {
            0L
        }
    }

    fun getMemoryPressure(): MemoryPressure {
        val averageUsage = getAverageMemoryUsage()
        val maxMemory = Runtime.getRuntime().maxMemory()

        return when (averageUsage.toDouble() / maxMemory) {
            in 0.0..0.5 -> MemoryPressure.LOW
            in 0.5..0.8 -> MemoryPressure.MEDIUM
            else -> MemoryPressure.HIGH
        }
    }

    enum class MemoryPressure {
        LOW, MEDIUM, HIGH
    }

    // Performance metrics collector
    data class NetworkMetrics(
        val requestCount: Int = 0,
        val averageResponseTime: Long = 0,
        val successRate: Double = 0.0,
        val bytesTransferred: Long = 0,
        val compressionRatio: Double = 0.0
    )

    private var networkMetrics = NetworkMetrics()

    fun updateMetrics(responseTime: Long, success: Boolean, bytesTransferred: Long, compressionRatio: Double) {
        networkMetrics = networkMetrics.copy(
            requestCount = networkMetrics.requestCount + 1,
            averageResponseTime = ((networkMetrics.averageResponseTime * (networkMetrics.requestCount - 1)) + responseTime) / networkMetrics.requestCount,
            successRate = ((networkMetrics.successRate * (networkMetrics.requestCount - 1)) + (if (success) 1.0 else 0.0)) / networkMetrics.requestCount,
            bytesTransferred = networkMetrics.bytesTransferred + bytesTransferred,
            compressionRatio = ((networkMetrics.compressionRatio * (networkMetrics.requestCount - 1)) + compressionRatio) / networkMetrics.requestCount
        )
    }

    fun getNetworkMetrics(): NetworkMetrics = networkMetrics

    // Reset metrics (useful for testing or periodic cleanup)
    fun resetMetrics() {
        networkMetrics = NetworkMetrics()
        memoryStats.clear()
    }
}

// Supporting classes
data class TimeoutConfig(
    val connectTimeout: Long,
    val readTimeout: Long,
    val writeTimeout: Long
)

class GzipRequestBody(private val originalBody: okhttp3.RequestBody) : okhttp3.RequestBody() {
    override fun contentType(): okhttp3.MediaType? = originalBody.contentType()

    override fun contentLength(): Long = -1 // Unknown compressed size

    override fun writeTo(sink: okio.BufferedSink) {
        val gzipSink = okio.GzipSink(sink).buffer()
        originalBody.writeTo(gzipSink)
        gzipSink.close()
    }
}
