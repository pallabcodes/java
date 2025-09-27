package com.example.ledgerpay.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ledgerpay.core.data.db.AppDatabase
import com.example.ledgerpay.core.network.CreateIntentRequest
import com.example.ledgerpay.core.network.CreateIntentResponse
import com.example.ledgerpay.core.network.PaymentsApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentsRepositoryTest {
    @Test
    fun create_persists_entity() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dao = db.paymentIntentDao()
        val api = object : PaymentsApi {
            override suspend fun createIntent(req: CreateIntentRequest) = CreateIntentResponse("pi_test", "succeeded")
        }
        val repo = PaymentsRepository(dao, api)
        val id = repo.createIntent(1000, "USD")
        assertEquals("pi_test", id)
        val back = dao.find("pi_test")
        assertEquals(1000, back?.amountMinor)
        assertEquals("USD", back?.currency)
    }
}
