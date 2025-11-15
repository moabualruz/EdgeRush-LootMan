package com.edgerush.datasync.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val adminModeConfig: AdminModeConfig,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        // Admin mode: bypass authentication
        if (adminModeConfig.isEnabled()) {
            val adminUser = AuthenticatedUser.adminModeUser()
            val authentication = createAuthentication(adminUser)
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
        }

        // Extract token from Authorization header
        val token = extractToken(exchange)

        if (token != null && jwtService.validateToken(token)) {
            try {
                val user = jwtService.extractUser(token)
                val authentication = createAuthentication(user)

                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            } catch (e: Exception) {
                // Invalid token, continue without authentication
            }
        }

        return chain.filter(exchange)
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        val authHeader = exchange.request.headers.getFirst("Authorization")
        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.substring(BEARER_PREFIX_LENGTH)
        } else {
            null
        }
    }

    companion object {
        private const val BEARER_PREFIX_LENGTH = 7 // Length of "Bearer "
    }

    private fun createAuthentication(user: AuthenticatedUser): UsernamePasswordAuthenticationToken {
        val authorities = user.roles.map { SimpleGrantedAuthority(it) }
        return UsernamePasswordAuthenticationToken(user, null, authorities)
    }
}
