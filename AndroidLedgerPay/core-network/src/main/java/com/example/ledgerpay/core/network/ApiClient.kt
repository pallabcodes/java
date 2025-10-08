package com.example.ledgerpay.core.network

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Duration

object ApiClient {
    fun retrofit(baseUrl: String): Retrofit {
        val pinner = CertificatePinner.Builder()
            // .add("example.org", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .build()

        val client = OkHttpClient.Builder()
            .certificatePinner(pinner)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .callTimeout(Duration.ofSeconds(10))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
}