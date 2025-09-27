package com.example.ledgerpay.feature.payments.vm

import com.example.ledgerpay.core.data.PaymentsRepository
import com.example.ledgerpay.core.data.db.PaymentIntentEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeRepo : PaymentsRepository(
    dao = object : com.example.ledgerpay.core.data.db.PaymentIntentDao {
        override suspend fun upsert(e: PaymentIntentEntity) {}
        override suspend fun find(id: String): PaymentIntentEntity? = null
        override suspend fun list(): List<PaymentIntentEntity> = listOf(PaymentIntentEntity("pi_1000_USD", 1000, "USD", "succeeded"))
    },
    api = object : com.example.ledgerpay.core.network.PaymentsApi {
        override suspend fun createIntent(req: com.example.ledgerpay.core.network.CreateIntentRequest) = com.example.ledgerpay.core.network.CreateIntentResponse("pi_1000_USD", "succeeded")
    }
) {}

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentsViewModelTest {
    @Test
    fun create_updates_state_and_list() = runTest {
        val vm = PaymentsViewModel(FakeRepo())
        vm.create(1000, "USD")
        assertEquals("pi_1000_USD", vm.intentId.value)
        assertEquals(1, vm.recent.value.size)
    }
}
