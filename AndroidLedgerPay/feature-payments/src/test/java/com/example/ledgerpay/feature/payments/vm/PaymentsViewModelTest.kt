package com.example.ledgerpay.feature.payments.vm

import com.example.ledgerpay.core.data.PaymentsRepository
import com.example.ledgerpay.core.data.db.PaymentIntentEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class PaymentsViewModelTest {

    private lateinit var viewModel: PaymentsViewModel
    private lateinit var mockRepository: PaymentsRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk()
        viewModel = PaymentsViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() = runTest {
        val state = viewModel.state.first()
        assertTrue(state is UiState.Idle)
    }

    @Test
    fun `createPayment with valid inputs succeeds`() = runTest {
        // Given
        val testId = "pi_test123"
        val amount = 1000L
        val currency = "USD"
        val testEntity = PaymentIntentEntity(testId, amount, currency, "requires_payment_method")

        coEvery { mockRepository.createIntent(amount, currency) } returns
            PaymentsRepository.Result.Success(testId)
        coEvery { mockRepository.listRecent() } returns
            PaymentsRepository.Result.Success(listOf(testEntity))

        // When
        viewModel.createPayment(amount, currency)

        // Then
        val finalState = viewModel.state.first()
        assertTrue(finalState is UiState.Success)
        assertEquals(testId, viewModel.intentId.first())
        assertEquals(listOf(testEntity), viewModel.recent.first())
    }

    @Test
    fun `createPayment with invalid amount shows error`() = runTest {
        // When
        viewModel.createPayment(-100L, "USD")

        // Then
        val state = viewModel.state.first()
        assertTrue(state is UiState.Error)
        assertEquals("Invalid payment amount", (state as UiState.Error).message)
    }

    @Test
    fun `createPayment with amount too large shows error`() = runTest {
        // When
        viewModel.createPayment(2_000_000_00L, "USD") // $20,000 > $10,000 max

        // Then
        val state = viewModel.state.first()
        assertTrue(state is UiState.Error)
        assertEquals("Invalid payment amount", (state as UiState.Error).message)
    }

    @Test
    fun `createPayment with invalid currency shows error`() = runTest {
        // When
        viewModel.createPayment(1000L, "INVALID")

        // Then
        val state = viewModel.state.first()
        assertTrue(state is UiState.Error)
        assertEquals("Invalid currency code", (state as UiState.Error).message)
    }

    @Test
    fun `createPayment with lowercase currency shows error`() = runTest {
        // When
        viewModel.createPayment(1000L, "usd")

        // Then
        val state = viewModel.state.first()
        assertTrue(state is UiState.Error)
        assertEquals("Invalid currency code", (state as UiState.Error).message)
    }

    @Test
    fun `createPayment handles repository error gracefully`() = runTest {
        // Given
        coEvery { mockRepository.createIntent(1000L, "USD") } returns
            PaymentsRepository.Result.Error(RuntimeException("Network error"))

        // When
        viewModel.createPayment(1000L, "USD")

        // Then
        val state = viewModel.state.first()
        assertTrue(state is UiState.Error)
        assertEquals("An error occurred. Please try again.", (state as UiState.Error).message)
        assertNull(viewModel.intentId.first())
    }

    @Test
    fun `createPayment handles security exception`() = runTest {
        // Given
        coEvery { mockRepository.createIntent(1000L, "USD") } returns
            PaymentsRepository.Result.Error(SecurityException("Not authenticated"))

        // When
        viewModel.createPayment(1000L, "USD")

        // Then
        val state = viewModel.state.first()
        assertTrue(state is UiState.Error)
        assertEquals("Authentication required. Please log in again.", (state as UiState.Error).message)
    }

    @Test
    fun `createPayment handles network timeout`() = runTest {
        // Given
        coEvery { mockRepository.createIntent(1000L, "USD") } returns
            PaymentsRepository.Result.Error(RuntimeException("Request timeout"))

        // When
        viewModel.createPayment(1000L, "USD")

        // Then
        val state = viewModel.state.first()
        assertTrue(state is UiState.Error)
        assertEquals("Request timed out. Please try again.", (state as UiState.Error).message)
    }

    @Test
    fun `clearError resets error state to idle`() = runTest {
        // Given - set error state
        viewModel.createPayment(-100L, "USD")
        assertTrue(viewModel.state.first() is UiState.Error)

        // When
        viewModel.clearError()

        // Then
        val state = viewModel.state.first()
        assertTrue(state is UiState.Idle)
    }

    @Test
    fun `clearError does nothing if not in error state`() = runTest {
        // Given - idle state
        assertTrue(viewModel.state.first() is UiState.Idle)

        // When
        viewModel.clearError()

        // Then
        val state = viewModel.state.first()
        assertTrue(state is UiState.Idle)
    }
}