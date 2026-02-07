package com.example.ledgerpay.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaymentIntentDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: PaymentIntentDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.paymentIntentDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun upsert_and_find_and_list() = runTest {
        val e = PaymentIntentEntity("pi_test", 1000, "USD", "succeeded")
        dao.upsert(e)
        val found = dao.find("pi_test")
        assertEquals(1000L, found?.amountMinor)
        assertEquals("USD", found?.currency)
        val list = dao.list()
        assertEquals(1, list.size)
    }
}
