package com.example.ledgerpay.core.data

import com.example.ledgerpay.core.data.db.PaymentIntentDao
import com.example.ledgerpay.core.data.db.PaymentIntentEntity
import com.example.ledgerpay.core.data.telemetry.Monitoring
import com.example.ledgerpay.core.network.CreateIntentRequest
import com.example.ledgerpay.core.network.CreateIntentResponse
import com.example.ledgerpay.core.network.PaymentsApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
class PaymentsRepositoryTest {

    private lateinit var repository: PaymentsRepository
    private lateinit var mockDao: PaymentIntentDao
    private lateinit var mockApi: PaymentsApi
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testMonitoring = object : Monitoring {
        override fun log(message: String) = Unit

        override fun logUserAction(event: String, attributes: Map<String, Any?>) = Unit

        override fun logSecurityEvent(event: String, attributes: Map<String, Any?>) = Unit

        override fun logPaymentEvent(event: String, paymentId: String?, amount: Long?, currency: String?) = Unit

        override fun logBusinessMetric(name: String, value: Number) = Unit

        override fun logError(error: Throwable, context: String?, userVisible: Boolean) = Unit

        override suspend fun <T> measurePerformance(operation: String, block: suspend () -> T): T = block()
    }

    @Before
    fun setUp() {
        mockDao = mockk()
        mockApi = mockk()
        repository = PaymentsRepository(mockDao, mockApi, testMonitoring, testDispatcher)
    }

    @Test
    fun `createIntent with valid inputs succeeds and saves to database`() = runTest {
        // Given
        val amount = 1000L
        val currency = "USD"
        val response = CreateIntentResponse("pi_test123", "requires_payment_method")

        coEvery { mockApi.createIntent(CreateIntentRequest(amount, currency)) } returns response
        coEvery { mockDao.upsert(any()) } returns Unit

        // When
        val result = repository.createIntent(amount, currency)

        // Then
        assertTrue(result is PaymentsRepository.Result.Success)
        assertEquals("pi_test123", (result as PaymentsRepository.Result.Success).data)

        coVerify {
            mockApi.createIntent(CreateIntentRequest(amount, currency))
            mockDao.upsert(PaymentIntentEntity("pi_test123", amount, currency, "requires_payment_method"))
        }
    }

    @Test
    fun `createIntent validates amount - negative amount throws exception`() = runTest {
        // When & Then
        val result = repository.createIntent(-100L, "USD")
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertTrue(error.exception.message?.contains("must be positive") == true)
    }

    @Test
    fun `createIntent validates amount - zero amount throws exception`() = runTest {
        // When & Then
        val result = repository.createIntent(0L, "USD")
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertTrue(error.exception.message?.contains("must be positive") == true)
    }

    @Test
    fun `createIntent validates amount - too large throws exception`() = runTest {
        // When & Then
        val result = repository.createIntent(2_000_000_00L, "USD") // $20,000
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertTrue(error.exception.message?.contains("exceeds maximum") == true)
    }

    @Test
    fun `createIntent validates currency - empty throws exception`() = runTest {
        // When & Then
        val result = repository.createIntent(1000L, "")
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertTrue(error.exception.message?.contains("Currency is required") == true)
    }

    @Test
    fun `createIntent validates currency - wrong length throws exception`() = runTest {
        // When & Then
        val result = repository.createIntent(1000L, "US")
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertTrue(error.exception.message?.contains("Invalid currency format") == true)
    }

    @Test
    fun `createIntent validates currency - invalid code throws exception`() = runTest {
        // When & Then
        val result = repository.createIntent(1000L, "US1")
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertTrue(error.exception.message?.contains("Invalid currency format") == true)
    }

    @Test
    fun `createIntent handles network errors gracefully`() = runTest {
        // Given
        coEvery { mockApi.createIntent(any()) } throws IOException("Network error")

        // When
        val result = repository.createIntent(1000L, "USD")

        // Then
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertEquals("Network error", error.exception.message)
    }

    @Test
    fun `createIntent handles HTTP 401 as security error`() = runTest {
        // Given
        val response = Response.error<CreateIntentResponse>(
            401,
            "{}".toResponseBody("application/json".toMediaType())
        )
        coEvery { mockApi.createIntent(any()) } throws HttpException(response)

        // When
        val result = repository.createIntent(1000L, "USD")

        // Then
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertTrue(error.exception is SecurityException)
        assertEquals("Authentication required", error.exception.message)
    }

    @Test
    fun `createIntent handles HTTP 403 as security error`() = runTest {
        // Given
        val response = Response.error<CreateIntentResponse>(
            403,
            "{}".toResponseBody("application/json".toMediaType())
        )
        coEvery { mockApi.createIntent(any()) } throws HttpException(response)

        // When
        val result = repository.createIntent(1000L, "USD")

        // Then
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertTrue(error.exception is SecurityException)
        assertEquals("Access denied", error.exception.message)
    }

    @Test
    fun `createIntent handles HTTP 429 as rate limit error`() = runTest {
        // Given
        val response = Response.error<CreateIntentResponse>(
            429,
            "{}".toResponseBody("application/json".toMediaType())
        )
        coEvery { mockApi.createIntent(any()) } throws HttpException(response)

        // When
        val result = repository.createIntent(1000L, "USD")

        // Then
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertEquals("Rate limit exceeded", error.exception.message)
    }

    @Test
    fun `createIntent handles server errors gracefully`() = runTest {
        // Given
        val response = Response.error<CreateIntentResponse>(
            500,
            "{}".toResponseBody("application/json".toMediaType())
        )
        coEvery { mockApi.createIntent(any()) } throws HttpException(response)

        // When
        val result = repository.createIntent(1000L, "USD")

        // Then
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertEquals("Server error", error.exception.message)
    }

    @Test
    fun `listRecent returns successful result with entities`() = runTest {
        // Given
        val entities = listOf(
            PaymentIntentEntity("pi_1", 1000L, "USD", "succeeded"),
            PaymentIntentEntity("pi_2", 2000L, "EUR", "pending")
        )
        coEvery { mockDao.list() } returns entities

        // When
        val result = repository.listRecent()

        // Then
        assertTrue(result is PaymentsRepository.Result.Success)
        assertEquals(entities, (result as PaymentsRepository.Result.Success).data)
    }

    @Test
    fun `listRecent handles database errors gracefully`() = runTest {
        // Given
        coEvery { mockDao.list() } throws RuntimeException("Database error")

        // When
        val result = repository.listRecent()

        // Then
        assertTrue(result is PaymentsRepository.Result.Error)
        val error = result as PaymentsRepository.Result.Error
        assertEquals("Database error", error.exception.message)
    }
}
