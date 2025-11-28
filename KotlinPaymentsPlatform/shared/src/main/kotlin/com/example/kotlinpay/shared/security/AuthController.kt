package com.example.kotlinpay.shared.security

import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val jwtTokenService: JwtTokenService,
    private val passwordEncoder: PasswordEncoder
) {

    // Simple in-memory user store for demo purposes
    // In production, this would be a proper user management service
    private val users = mapOf(
        "admin" to User("admin", passwordEncoder.encode("admin123"), listOf("ADMIN", "USER")),
        "payments_user" to User("payments_user", passwordEncoder.encode("pay123"), listOf("SERVICE", "USER")),
        "risk_user" to User("risk_user", passwordEncoder.encode("risk123"), listOf("SERVICE", "USER")),
        "ledger_user" to User("ledger_user", passwordEncoder.encode("ledger123"), listOf("SERVICE", "USER")),
        "gateway_user" to User("gateway_user", passwordEncoder.encode("gw123"), listOf("GATEWAY", "USER")),
        "merchant1" to User("merchant1", passwordEncoder.encode("merchant123"), listOf("MERCHANT", "USER")),
        "customer1" to User("customer1", passwordEncoder.encode("customer123"), listOf("CUSTOMER", "USER"))
    )

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Map<String, Any>> {
        val user = users[request.username]

        if (user == null || !passwordEncoder.matches(request.password, user.password)) {
            val error = mapOf(
                "error" to "Invalid credentials",
                "message" to "Username or password is incorrect",
                "timestamp" to System.currentTimeMillis()
            )
            return ResponseEntity.status(401).body(error)
        }

        // Generate tokens
        val claims = mapOf("username" to user.username, "roles" to user.roles)
        val accessToken = jwtTokenService.generateAccessToken(user.username, claims)
        val refreshToken = jwtTokenService.generateRefreshToken(user.username)

        val response = mapOf(
            "accessToken" to accessToken,
            "refreshToken" to refreshToken,
            "tokenType" to "Bearer",
            "expiresIn" to 3600,
            "user" to mapOf(
                "username" to user.username,
                "roles" to user.roles
            )
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/validate")
    fun validate(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val token = request["token"]

        if (token == null || !jwtTokenService.validateToken(token)) {
            val response = mapOf("valid" to false)
            return ResponseEntity.ok(response)
        }

        val username = jwtTokenService.getSubjectFromToken(token)
        val claims = jwtTokenService.getClaimsFromToken(token)

        val response = mapOf(
            "valid" to true,
            "username" to username,
            "roles" to claims.getOrDefault("roles", emptyList<String>())
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val refreshToken = request["refreshToken"]

        if (refreshToken == null || !jwtTokenService.validateToken(refreshToken)) {
            val error = mapOf(
                "error" to "Invalid refresh token",
                "timestamp" to System.currentTimeMillis()
            )
            return ResponseEntity.status(401).body(error)
        }

        val tokenType = jwtTokenService.getTokenType(refreshToken)
        if ("refresh" != tokenType) {
            val error = mapOf(
                "error" to "Token is not a refresh token",
                "timestamp" to System.currentTimeMillis()
            )
            return ResponseEntity.status(401).body(error)
        }

        val username = jwtTokenService.getSubjectFromToken(refreshToken)
        if (username == null) {
            val error = mapOf(
                "error" to "Invalid refresh token",
                "timestamp" to System.currentTimeMillis()
            )
            return ResponseEntity.status(401).body(error)
        }

        val user = users[username]
        if (user == null) {
            val error = mapOf(
                "error" to "User not found",
                "timestamp" to System.currentTimeMillis()
            )
            return ResponseEntity.status(401).body(error)
        }

        // Generate new access token
        val claims = mapOf("username" to user.username, "roles" to user.roles)
        val newAccessToken = jwtTokenService.generateAccessToken(user.username, claims)

        val response = mapOf(
            "accessToken" to newAccessToken,
            "tokenType" to "Bearer",
            "expiresIn" to 3600
        )

        return ResponseEntity.ok(response)
    }

    data class LoginRequest(
        @field:NotBlank(message = "Username is required")
        val username: String,
        @field:NotBlank(message = "Password is required")
        val password: String
    )

    data class User(
        val username: String,
        val password: String,
        val roles: List<String>
    )
}
