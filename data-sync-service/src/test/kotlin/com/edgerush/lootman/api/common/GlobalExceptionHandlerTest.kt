package com.edgerush.lootman.api.common

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.GuildNotFoundException
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.ItemNotFoundException
import com.edgerush.lootman.domain.shared.LootBanActiveException
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.RaiderNotFoundException
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Unit tests for GlobalExceptionHandler.
 *
 * Tests exception handling logic and response formatting.
 */
class GlobalExceptionHandlerTest : UnitTest() {
    private val handler = GlobalExceptionHandler()

    @Nested
    inner class DomainExceptionHandlerTests {

        @Test
        fun `handleRaiderNotFoundException should return 404 Not Found`() {
            // Arrange
            val exception = RaiderNotFoundException(RaiderId(123L))

            // Act
            val response = handler.handleRaiderNotFoundException(exception)

            // Assert
            response.statusCode shouldBe HttpStatus.NOT_FOUND
            response.body?.status shouldBe 404
            response.body?.error shouldBe "Not Found"
            response.body?.message shouldBe "Raider not found: 123"
        }

        @Test
        fun `handleGuildNotFoundException should return 404 Not Found`() {
            // Arrange
            val exception = GuildNotFoundException(GuildId("guild-456"))

            // Act
            val response = handler.handleGuildNotFoundException(exception)

            // Assert
            response.statusCode shouldBe HttpStatus.NOT_FOUND
            response.body?.status shouldBe 404
            response.body?.error shouldBe "Not Found"
            response.body?.message shouldBe "Guild not found: guild-456"
        }

        @Test
        fun `handleItemNotFoundException should return 404 Not Found`() {
            // Arrange
            val exception = ItemNotFoundException(ItemId(99999L))

            // Act
            val response = handler.handleItemNotFoundException(exception)

            // Assert
            response.statusCode shouldBe HttpStatus.NOT_FOUND
            response.body?.status shouldBe 404
            response.body?.error shouldBe "Not Found"
            response.body?.message shouldBe "Item not found: 99999"
        }

        @Test
        fun `handleLootBanActiveException should return 409 Conflict`() {
            // Arrange
            val ban1 = LootBan.create(
                raiderId = RaiderId(123L),
                guildId = GuildId("guild-456"),
                reason = "Test ban 1",
                expiresAt = null
            )
            val ban2 = LootBan.create(
                raiderId = RaiderId(123L),
                guildId = GuildId("guild-456"),
                reason = "Test ban 2",
                expiresAt = null
            )
            val exception = LootBanActiveException(RaiderId(123L), listOf(ban1, ban2))

            // Act
            val response = handler.handleLootBanActiveException(exception)

            // Assert
            response.statusCode shouldBe HttpStatus.CONFLICT
            response.body?.status shouldBe 409
            response.body?.error shouldBe "Conflict"
            response.body?.message shouldBe "Raider 123 has 2 active loot ban(s)"
        }

        @Test
        fun `handleLootBanActiveException should handle single ban`() {
            // Arrange
            val ban = LootBan.create(
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                reason = "Single ban",
                expiresAt = null
            )
            val exception = LootBanActiveException(RaiderId(456L), listOf(ban))

            // Act
            val response = handler.handleLootBanActiveException(exception)

            // Assert
            response.statusCode shouldBe HttpStatus.CONFLICT
            response.body?.message shouldBe "Raider 456 has 1 active loot ban(s)"
        }
    }

    @Test
    fun `handleIllegalArgumentException should return 400 Bad Request with message`() {
        // Arrange
        val exception = IllegalArgumentException("Invalid parameter value")

        // Act
        val response = handler.handleIllegalArgumentException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.status shouldBe 400
        response.body?.error shouldBe "Bad Request"
        response.body?.message shouldBe "Invalid parameter value"
    }

    @Test
    fun `handleIllegalArgumentException should use default message when exception message is null`() {
        // Arrange
        val exception = IllegalArgumentException()

        // Act
        val response = handler.handleIllegalArgumentException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.status shouldBe 400
        response.body?.error shouldBe "Bad Request"
        response.body?.message shouldBe "Invalid request parameters"
    }

    @Test
    fun `handleIllegalStateException should return 409 Conflict with message`() {
        // Arrange
        val exception = IllegalStateException("Resource is in invalid state")

        // Act
        val response = handler.handleIllegalStateException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body?.status shouldBe 409
        response.body?.error shouldBe "Conflict"
        response.body?.message shouldBe "Resource is in invalid state"
    }

    @Test
    fun `handleIllegalStateException should use default message when exception message is null`() {
        // Arrange
        val exception = IllegalStateException()

        // Act
        val response = handler.handleIllegalStateException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body?.status shouldBe 409
        response.body?.error shouldBe "Conflict"
        response.body?.message shouldBe "Operation cannot be completed due to current state"
    }

    @Test
    fun `handleNoSuchElementException should return 404 Not Found with message`() {
        // Arrange
        val exception = NoSuchElementException("Raider not found with id: 123")

        // Act
        val response = handler.handleNoSuchElementException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body?.status shouldBe 404
        response.body?.error shouldBe "Not Found"
        response.body?.message shouldBe "Raider not found with id: 123"
    }

    @Test
    fun `handleNoSuchElementException should use default message when exception message is null`() {
        // Arrange
        val exception = NoSuchElementException()

        // Act
        val response = handler.handleNoSuchElementException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body?.status shouldBe 404
        response.body?.error shouldBe "Not Found"
        response.body?.message shouldBe "Resource not found"
    }

    @Test
    fun `handleException should return 400 for MissingServletRequestParameterException`() {
        // Arrange
        val exception = MissingServletRequestParameterException("Required parameter 'guildId' is missing")

        // Act
        val response = handler.handleException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.status shouldBe 400
        response.body?.error shouldBe "Bad Request"
        response.body?.message shouldBe "Required parameter 'guildId' is missing"
    }

    @Test
    fun `handleException should return 400 for MethodArgumentTypeMismatchException`() {
        // Arrange
        val exception = MethodArgumentTypeMismatchException("Failed to convert value 'abc' to Long")

        // Act
        val response = handler.handleException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.status shouldBe 400
        response.body?.error shouldBe "Bad Request"
        response.body?.message shouldBe "Failed to convert value 'abc' to Long"
    }

    @Test
    fun `handleException should return 400 for BindException`() {
        // Arrange
        val exception = BindException("Validation failed for request body")

        // Act
        val response = handler.handleException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.status shouldBe 400
        response.body?.error shouldBe "Bad Request"
        response.body?.message shouldBe "Validation failed for request body"
    }

    @Test
    fun `handleException should return 500 for generic exception`() {
        // Arrange
        val exception = RuntimeException("Database connection failed")

        // Act
        val response = handler.handleException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.status shouldBe 500
        response.body?.error shouldBe "Internal Server Error"
        response.body?.message shouldBe "An unexpected error occurred"
    }

    @Test
    fun `handleException should return 500 for NullPointerException`() {
        // Arrange
        val exception = NullPointerException("Unexpected null value")

        // Act
        val response = handler.handleException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.status shouldBe 500
        response.body?.error shouldBe "Internal Server Error"
        response.body?.message shouldBe "An unexpected error occurred"
    }

    @Test
    fun `handleException should return 400 when exception message is null for binding exception`() {
        // Arrange
        val exception = MissingServletRequestParameterException(null)

        // Act
        val response = handler.handleException(exception)

        // Assert
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.status shouldBe 400
        response.body?.error shouldBe "Bad Request"
        response.body?.message shouldBe "Invalid request parameters"
    }
}

/**
 * Test helper class mimicking MissingServletRequestParameterException.
 */
private class MissingServletRequestParameterException(
    override val message: String?,
) : Exception(message)

/**
 * Test helper class mimicking MethodArgumentTypeMismatchException.
 */
private class MethodArgumentTypeMismatchException(
    override val message: String?,
) : Exception(message)

/**
 * Test helper class mimicking BindException.
 */
private class BindException(
    override val message: String?,
) : Exception(message)
