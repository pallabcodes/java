package com.example.ledgerpay.core.network

import retrofit2.http.Body
import retrofit2.http.POST

data class CreateIntentRequest(val amountMinor: Long, val currency: String)
data class CreateIntentResponse(val id: String, val status: String)

interface PaymentsApi {
    @POST("/payment_intents")
    suspend fun createIntent(@Body req: CreateIntentRequest): CreateIntentResponse
}
