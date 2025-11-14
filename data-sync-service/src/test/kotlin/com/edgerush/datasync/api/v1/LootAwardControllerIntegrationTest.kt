package com.edgerush.datasync.api.v1

import com.edgerush.datasync.test.base.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Integration tests for LootAwardController (legacy CRUD endpoints).
 *
 * These tests verify backward compatibility with the existing CRUD API.
 * The legacy controller continues to use the CRUD service for full backward compatibility.
 */
class LootAwardControllerIntegrationTest : IntegrationTest() {
    @Test
    fun `should return 200 OK for findAll endpoint`() {
        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/loot-awards",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `should maintain backward compatibility for pagination parameters`() {
        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/loot-awards?page=0&size=10",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `should maintain backward compatibility for sorting parameters`() {
        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/loot-awards?page=0&size=10&sort=id,desc",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }
}
