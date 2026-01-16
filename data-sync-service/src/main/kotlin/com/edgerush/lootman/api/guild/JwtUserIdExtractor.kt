package com.edgerush.lootman.api.guild

import com.edgerush.lootman.api.auth.AuthenticationService
import com.edgerush.lootman.domain.auth.model.UserId
import org.springframework.stereotype.Component

/**
 * JWT-based implementation of UserIdExtractor.
 *
 * Extracts the user ID from the Authorization header by validating
 * the JWT token.
 */
@Component
class JwtUserIdExtractor(
    private val authenticationService: AuthenticationService,
) : UserIdExtractor {
    override fun extractUserId(authorization: String): UserId {
        val token = extractBearerToken(authorization)
        return authenticationService.validateToken(token)
            ?: throw IllegalArgumentException("Invalid or expired token")
    }

    private fun extractBearerToken(authorization: String): String {
        if (!authorization.startsWith("Bearer ", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid Authorization header format")
        }
        return authorization.substring(7)
    }
}
