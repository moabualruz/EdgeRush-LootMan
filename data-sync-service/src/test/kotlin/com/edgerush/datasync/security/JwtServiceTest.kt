package com.edgerush.datasync.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JwtServiceTest {
    private val jwtProperties =
        JwtProperties(
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-security-purposes",
            expirationMs = 3600000,
            issuer = "test-issuer",
        )

    private val jwtService = JwtService(jwtProperties)

    @Test
    fun `generateToken should create valid JWT`() {
        // Given
        val user =
            AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN"),
                guildIds = listOf("guild-1", "guild-2"),
            )

        // When
        val token = jwtService.generateToken(user)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
    }

    @Test
    fun `validateToken should return true for valid token`() {
        // Given
        val user =
            AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN"),
            )
        val token = jwtService.generateToken(user)

        // When
        val isValid = jwtService.validateToken(token)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `validateToken should return false for invalid token`() {
        // When
        val isValid = jwtService.validateToken("invalid-token")

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `extractUser should return correct user data`() {
        // Given
        val user =
            AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN", "SYSTEM_ADMIN"),
                guildIds = listOf("guild-1", "guild-2"),
            )
        val token = jwtService.generateToken(user)

        // When
        val extractedUser = jwtService.extractUser(token)

        // Then
        assertEquals(user.id, extractedUser.id)
        assertEquals(user.username, extractedUser.username)
        assertEquals(user.roles, extractedUser.roles)
        assertEquals(user.guildIds, extractedUser.guildIds)
        assertFalse(extractedUser.isAdminMode)
    }
}
