package com.example.kotlinpay.risk

import com.example.kotlinpay.shared.security.AuthController
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    @Test
    fun `should allow login and return tokens`() {
        val loginRequest = AuthController.LoginRequest("admin", "admin123")

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("admin"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
    }

    @Test
    fun `should reject invalid login credentials`() {
        val loginRequest = AuthController.LoginRequest("admin", "wrongpassword")

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.error").value("Invalid credentials"))
    }

    @Test
    fun `should require authentication for protected endpoints`() {
        // Try to access protected endpoint without authentication
        mockMvc.perform(get("/api/v1/risk/decisions"))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should allow authenticated access to risk evaluation`() {
        // Login first
        val loginRequest = AuthController.LoginRequest("risk_user", "risk123")

        val loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString

        // Extract access token
        val accessToken = extractAccessToken(loginResponse)

        // Create risk evaluation request
        val riskRequest = mapOf(
            "paymentId" to "pay_test_123",
            "amount" to 100.00,
            "currency" to "USD",
            "customerId" to "cust_test",
            "merchantId" to "merc_test",
            "cardLastFour" to "4242",
            "countryCode" to "US",
            "ipAddress" to "127.0.0.1",
            "userAgent" to "test-agent"
        )

        // Access protected endpoint with token
        mockMvc.perform(post("/api/v1/risk/decisions")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(riskRequest)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.paymentId").value("pay_test_123"))
                .andExpect(jsonPath("$.decision").exists())
                .andExpect(jsonPath("$.riskScore").exists())
    }

    @Test
    fun `should validate JWT token through auth endpoint`() {
        // Login first
        val loginRequest = AuthController.LoginRequest("user1", "user123")

        val loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString

        // Extract access token
        val accessToken = extractAccessToken(loginResponse)

        // Validate token
        val validateRequest = mapOf("token" to accessToken)
        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.role").value("USER"))
    }

    @Test
    fun `should reject invalid JWT token`() {
        val validateRequest = mapOf("token" to "invalid.jwt.token")
        mockMvc.perform(post("/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.valid").value(false))
    }

    @Test
    fun `should allow health endpoint without authentication`() {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("risk-service"))
    }

    private fun extractAccessToken(response: String): String {
        // Simple JSON parsing for test
        val start = response.indexOf("\"accessToken\":\"") + 15
        val end = response.indexOf("\"", start)
        return response.substring(start, end)
    }
}
