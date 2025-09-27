package com.example.ledgerpay.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(
        val pinner = okhttp3.CertificatePinner.Builder()
            // .add("example.org", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .build()
        val client = OkHttpClient.Builder()
            .certificatePinner(pinner)
            .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply { level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC })
            .callTimeout(java.time.Duration.ofSeconds(10))
            .build()
            .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply { level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC }).callTimeout(java.time.Duration.ofSeconds(10)).build())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
}