
package com.example.ledgerpay.core.data

import com.example.ledgerpay.core.data.db.PaymentIntentDao
import com.example.ledgerpay.core.data.db.PaymentIntentEntity
import com.example.ledgerpay.core.network.CreateIntentRequest
import com.example.ledgerpay.core.network.PaymentsApi

class PaymentsRepository(
    private val dao: PaymentIntentDao,
    private val api: PaymentsApi
) {
    suspend fun createIntent(amountMinor: Long, currency: String): String {
        val res = api.createIntent(CreateIntentRequest(amountMinor, currency))
        dao.upsert(PaymentIntentEntity(res.id, amountMinor, currency, status = res.status))
        return res.id
    }
    suspend fun listRecent(): List<PaymentIntentEntity> = dao.list()
}
