package com.example.kotlinpay.shared.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.Key
import java.time.Instant
import java.util.*

@Service
class JwtTokenService(
    @Value("\${jwt.secret:kotlin-payments-jwt-secret-key}")
    private val secret: String,
    @Value("\${jwt.access-token-expiry:3600000}")
    private val accessTokenExpiryMs: Long,
    @Value("\${jwt.refresh-token-expiry:86400000}")
    private val refreshTokenExpiryMs: Long
) {
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun generateAccessToken(subject: String, claims: Map<String, Any> = emptyMap()): String {
        val now = Instant.now()
        val tokenClaims = mutableMapOf<String, Any>()
        tokenClaims.putAll(claims)
        tokenClaims["type"] = "access"
        tokenClaims["issued_at"] = now.toEpochMilli()

        return Jwts.builder()
            .setSubject(subject)
            .addClaims(tokenClaims)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusMillis(accessTokenExpiryMs)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun generateRefreshToken(subject: String): String {
        val now = Instant.now()
        return Jwts.builder()
            .setSubject(subject)
            .claim("type", "refresh")
            .claim("issued_at", now.toEpochMilli())
            .setSubject(subject)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusMillis(refreshTokenExpiryMs)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getSubjectFromToken(token: String): String? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .subject
        } catch (e: Exception) {
            null
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getTokenType(token: String): String? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .get("type", String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getClaimsFromToken(token: String): Map<String, Any> {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun getClaim(token: String, claimName: String): Any? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .get(claimName)
        } catch (e: Exception) {
            null
        }
    }
}
