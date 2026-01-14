package com.edgerush.lootman.api.graphql.error

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.GuildNotFoundException
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.ItemNotFoundException
import com.edgerush.lootman.domain.shared.LootBanActiveException
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.RaiderNotFoundException
import graphql.GraphQLError
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.ResultPath
import graphql.language.SourceLocation
import graphql.schema.DataFetchingEnvironment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for GraphQLExceptionHandler.
 *
 * Tests that domain exceptions are correctly transformed into GraphQL errors
 * with appropriate error codes and messages.
 */
class GraphQLExceptionHandlerTest : UnitTest() {

    private lateinit var handler: GraphQLExceptionHandler

    @BeforeEach
    fun setup() {
        handler = GraphQLExceptionHandler()
    }

    @Nested
    inner class HandleNotFoundExceptions {

        @Test
        fun `should handle RaiderNotFoundException as NOT_FOUND error`() = runBlocking {
            // Arrange
            val exception = RaiderNotFoundException(RaiderId(123L))
            val params = createExceptionParams(exception)

            // Act
            val result = handler.handleException(params).get()

            // Assert
            val error = result.errors.first()
            error.message shouldContain "Raider not found"
            error.message shouldContain "123"
            getErrorCode(error) shouldBe "NOT_FOUND"
        }

        @Test
        fun `should handle GuildNotFoundException as NOT_FOUND error`() = runBlocking {
            // Arrange
            val exception = GuildNotFoundException(GuildId("test-guild"))
            val params = createExceptionParams(exception)

            // Act
            val result = handler.handleException(params).get()

            // Assert
            val error = result.errors.first()
            error.message shouldContain "Guild not found"
            error.message shouldContain "test-guild"
            getErrorCode(error) shouldBe "NOT_FOUND"
        }

        @Test
        fun `should handle ItemNotFoundException as NOT_FOUND error`() = runBlocking {
            // Arrange
            val exception = ItemNotFoundException(ItemId(12345L))
            val params = createExceptionParams(exception)

            // Act
            val result = handler.handleException(params).get()

            // Assert
            val error = result.errors.first()
            error.message shouldContain "Item not found"
            error.message shouldContain "12345"
            getErrorCode(error) shouldBe "NOT_FOUND"
        }

        @Test
        fun `should handle NoSuchElementException as NOT_FOUND error`() = runBlocking {
            // Arrange
            val exception = NoSuchElementException("Resource not found")
            val params = createExceptionParams(exception)

            // Act
            val result = handler.handleException(params).get()

            // Assert
            val error = result.errors.first()
            error.message shouldContain "Resource not found"
            getErrorCode(error) shouldBe "NOT_FOUND"
        }
    }

    @Nested
    inner class HandleValidationExceptions {

        @Test
        fun `should handle IllegalArgumentException as BAD_REQUEST error`() = runBlocking {
            // Arrange
            val exception = IllegalArgumentException("Invalid raider ID format")
            val params = createExceptionParams(exception)

            // Act
            val result = handler.handleException(params).get()

            // Assert
            val error = result.errors.first()
            error.message shouldContain "Invalid raider ID format"
            getErrorCode(error) shouldBe "BAD_REQUEST"
        }
    }

    @Nested
    inner class HandleConflictExceptions {

        @Test
        fun `should handle LootBanActiveException as CONFLICT error`() = runBlocking {
            // Arrange
            val exception = LootBanActiveException(RaiderId(123L), emptyList())
            val params = createExceptionParams(exception)

            // Act
            val result = handler.handleException(params).get()

            // Assert
            val error = result.errors.first()
            error.message shouldContain "active loot ban"
            getErrorCode(error) shouldBe "CONFLICT"
        }

        @Test
        fun `should handle IllegalStateException as CONFLICT error`() = runBlocking {
            // Arrange
            val exception = IllegalStateException("Cannot revoke already revoked award")
            val params = createExceptionParams(exception)

            // Act
            val result = handler.handleException(params).get()

            // Assert
            val error = result.errors.first()
            error.message shouldContain "Cannot revoke already revoked award"
            getErrorCode(error) shouldBe "CONFLICT"
        }
    }

    @Nested
    inner class HandleUnknownExceptions {

        @Test
        fun `should handle unknown exceptions as INTERNAL_ERROR`() = runBlocking {
            // Arrange
            val exception = RuntimeException("Unexpected database error")
            val params = createExceptionParams(exception)

            // Act
            val result = handler.handleException(params).get()

            // Assert
            val error = result.errors.first()
            error.message shouldBe "An unexpected error occurred"
            getErrorCode(error) shouldBe "INTERNAL_ERROR"
        }
    }

    // Helper functions
    private fun createExceptionParams(exception: Throwable): DataFetcherExceptionHandlerParameters {
        val environment = mockk<DataFetchingEnvironment>(relaxed = true)
        every { environment.field } returns mockk(relaxed = true) {
            every { sourceLocation } returns SourceLocation(1, 1)
        }

        val resultPath = ResultPath.rootPath().segment("testField")

        return mockk {
            every { this@mockk.exception } returns exception
            every { sourceLocation } returns SourceLocation(1, 1)
            every { this@mockk.path } returns resultPath
            every { dataFetchingEnvironment } returns environment
        }
    }

    private fun getErrorCode(error: GraphQLError): String? {
        return error.extensions?.get("code") as? String
    }
}
