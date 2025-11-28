package com.example.kotlinpay.shared.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val requestTokenHeader = request.getHeader("Authorization")

        var username: String? = null
        var jwtToken: String? = null

        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7)
            try {
                username = jwtTokenService.getSubjectFromToken(jwtToken)
            } catch (e: Exception) {
                logger.warn("Unable to get JWT Token or JWT Token has expired: ${e.message}")
            }
        } else {
            logger.debug("JWT Token does not begin with Bearer String")
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().authentication == null) {

            // Validate token
            if (jwtTokenService.validateToken(jwtToken!!)) {

                // Get token type to ensure it's an access token
                val tokenType = jwtTokenService.getTokenType(jwtToken)
                if ("access" == tokenType) {

                    // Extract claims and roles
                    val claims = jwtTokenService.getClaimsFromToken(jwtToken)

                    // Get roles from claims (default to USER if not specified)
                    val roles = claims.getOrDefault("roles", listOf("USER")) as? List<String> ?: listOf("USER")

                    // Create authorities from roles
                    val authorities = roles.map { role -> SimpleGrantedAuthority("ROLE_$role") }

                    // Create authentication token
                    val usernamePasswordAuthenticationToken =
                        UsernamePasswordAuthenticationToken(username, null, authorities)

                    usernamePasswordAuthenticationToken.details =
                        WebAuthenticationDetailsSource().buildDetails(request)

                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken

                    logger.debug("Set authentication for user: $username with roles: $roles")
                } else {
                    logger.warn("Invalid token type for authentication: $tokenType")
                }
            } else {
                logger.warn("Invalid JWT token for user: $username")
            }
        }

        chain.doFilter(request, response)
    }
}
