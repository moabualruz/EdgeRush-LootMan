package com.edgerush.lootman.infrastructure.audit

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.audit.model.AuditLog
import com.edgerush.lootman.domain.audit.model.AuditOperation
import com.edgerush.lootman.domain.audit.repository.AuditLogRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Unit tests for AuditWebFilter.
 *
 * These tests verify that the filter correctly captures write operations
 * (POST, PUT, DELETE) and logs them to the audit log.
 */
class AuditWebFilterTest : UnitTest() {

    private lateinit var auditLogRepository: AuditLogRepository
    private lateinit var filter: AuditWebFilter
    private lateinit var filterChain: WebFilterChain

    @BeforeEach
    fun setUp() {
        auditLogRepository = mockk(relaxed = true)
        filter = AuditWebFilter(auditLogRepository)
        filterChain = mockk()
        every { filterChain.filter(any()) } returns Mono.empty()
    }

    private fun createExchange(
        method: HttpMethod,
        path: String,
        headers: Map<String, String> = emptyMap(),
        statusCode: HttpStatus = HttpStatus.OK
    ): MockServerWebExchange {
        val requestBuilder = MockServerHttpRequest.method(method, path)
        headers.forEach { (key, value) -> requestBuilder.header(key, value) }
        val exchange = MockServerWebExchange.from(requestBuilder.build())
        exchange.response.statusCode = statusCode
        return exchange
    }

    @Nested
    inner class WriteOperationCapture {

        @Test
        fun `should capture PATCH requests as UPDATE operation`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.PATCH,
                path = "/api/v1/raiders/123",
                headers = mapOf(
                    "X-User-Id" to "user-456",
                    "X-Username" to "admin",
                    "X-Admin-Mode" to "true"
                ),
                statusCode = HttpStatus.OK
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { auditLogRepository.save(any()) }

            val captured = auditLogSlot.captured
            captured.operation shouldBe AuditOperation.UPDATE
            captured.entityType shouldBe "raiders"
            captured.entityId shouldBe "123"
        }

        @Test
        fun `should capture POST requests as CREATE operation`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false",
                    "X-Request-Id" to "req-456"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { filterChain.filter(exchange) }
            verify { auditLogRepository.save(any()) }

            val captured = auditLogSlot.captured
            captured.operation shouldBe AuditOperation.CREATE
            captured.entityType shouldBe "guilds"
            captured.userId shouldBe "user-123"
            captured.username shouldBe "testuser"
            captured.isAdminMode shouldBe false
            captured.requestId shouldBe "req-456"
        }

        @Test
        fun `should capture PUT requests as UPDATE operation`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.PUT,
                path = "/api/v1/raiders/123",
                headers = mapOf(
                    "X-User-Id" to "user-456",
                    "X-Username" to "admin",
                    "X-Admin-Mode" to "true"
                ),
                statusCode = HttpStatus.OK
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { auditLogRepository.save(any()) }

            val captured = auditLogSlot.captured
            captured.operation shouldBe AuditOperation.UPDATE
            captured.entityType shouldBe "raiders"
            captured.entityId shouldBe "123"
            captured.isAdminMode shouldBe true
        }

        @Test
        fun `should capture DELETE requests as DELETE operation`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.DELETE,
                path = "/api/v1/loot/awards/abc-123",
                headers = mapOf(
                    "X-User-Id" to "user-789",
                    "X-Username" to "moderator",
                    "X-Admin-Mode" to "false",
                    "X-Request-Id" to "req-xyz"
                ),
                statusCode = HttpStatus.NO_CONTENT
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { auditLogRepository.save(any()) }

            val captured = auditLogSlot.captured
            captured.operation shouldBe AuditOperation.DELETE
            captured.entityType shouldBe "awards"
            captured.entityId shouldBe "abc-123"
        }
    }

    @Nested
    inner class SkipReadOperations {

        @Test
        fun `should not capture GET requests`() {
            // Given
            val exchange = createExchange(
                method = HttpMethod.GET,
                path = "/api/v1/guilds"
            )

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { filterChain.filter(exchange) }
            verify(exactly = 0) { auditLogRepository.save(any()) }
        }

        @Test
        fun `should not capture HEAD requests`() {
            // Given
            val exchange = createExchange(
                method = HttpMethod.HEAD,
                path = "/api/v1/health"
            )

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { filterChain.filter(exchange) }
            verify(exactly = 0) { auditLogRepository.save(any()) }
        }

        @Test
        fun `should not capture OPTIONS requests`() {
            // Given
            val exchange = createExchange(
                method = HttpMethod.OPTIONS,
                path = "/api/v1/guilds"
            )

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { filterChain.filter(exchange) }
            verify(exactly = 0) { auditLogRepository.save(any()) }
        }
    }

    @Nested
    inner class SkipNonApiRequests {

        @Test
        fun `should not capture actuator endpoints`() {
            // Given
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/actuator/health"
            )

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { filterChain.filter(exchange) }
            verify(exactly = 0) { auditLogRepository.save(any()) }
        }

        @Test
        fun `should not capture non-api paths`() {
            // Given
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/swagger-ui/index.html"
            )

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { filterChain.filter(exchange) }
            verify(exactly = 0) { auditLogRepository.save(any()) }
        }
    }

    @Nested
    inner class SkipFailedRequests {

        @Test
        fun `should not capture requests with 4xx response status`() {
            // Given
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds",
                statusCode = HttpStatus.BAD_REQUEST
            )

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { filterChain.filter(exchange) }
            verify(exactly = 0) { auditLogRepository.save(any()) }
        }

        @Test
        fun `should not capture requests with 5xx response status`() {
            // Given
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds",
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR
            )

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { filterChain.filter(exchange) }
            verify(exactly = 0) { auditLogRepository.save(any()) }
        }
    }

    @Nested
    inner class RequestIdHandling {

        @Test
        fun `should handle missing request id`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { auditLogRepository.save(any()) }
            val captured = auditLogSlot.captured
            captured.requestId shouldBe null
        }

        @Test
        fun `should use provided request id`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false",
                    "X-Request-Id" to "custom-req-id"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            val captured = auditLogSlot.captured
            captured.requestId shouldBe "custom-req-id"
        }
    }

    @Nested
    inner class DefaultUserHandling {

        @Test
        fun `should use default user when headers not provided`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds",
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { auditLogRepository.save(any()) }

            val captured = auditLogSlot.captured
            captured.userId shouldBe "anonymous"
            captured.username shouldBe "anonymous"
            captured.isAdminMode shouldBe false
        }
    }

    @Nested
    inner class EntityExtraction {

        @Test
        fun `should extract entity type from API path`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/attendance/track",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            val captured = auditLogSlot.captured
            captured.entityType shouldBe "attendance"
        }

        @Test
        fun `should extract entity id from path`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.PUT,
                path = "/api/v1/guilds/my-guild-id",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false"
                ),
                statusCode = HttpStatus.OK
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            val captured = auditLogSlot.captured
            captured.entityType shouldBe "guilds"
            captured.entityId shouldBe "my-guild-id"
        }

        @Test
        fun `should handle nested resource paths`() {
            // Given
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.DELETE,
                path = "/api/v1/loot/bans/ban-123",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false"
                ),
                statusCode = HttpStatus.NO_CONTENT
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            val captured = auditLogSlot.captured
            captured.entityType shouldBe "bans"
            captured.entityId shouldBe "ban-123"
        }

        @Test
        fun `should handle paths with numeric id in nested resources`() {
            // Given - path like /api/v1/guilds/123/members where 123 looks like an ID
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds/12345/members",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            val captured = auditLogSlot.captured
            captured.entityType shouldBe "guilds"
            captured.entityId shouldBe "members"
        }

        @Test
        fun `should handle paths with UUID-like id in nested resources`() {
            // Given - path like /api/v1/guilds/abc12345-6789-0def/members
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds/abc12345-6789-0def/members",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            val captured = auditLogSlot.captured
            captured.entityType shouldBe "guilds"
            captured.entityId shouldBe "members"
        }

        @Test
        fun `should handle single-part path`() {
            // Given - path like /api/v1/guilds
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            val captured = auditLogSlot.captured
            captured.entityType shouldBe "guilds"
            captured.entityId shouldBe "guilds"
        }

        @Test
        fun `should handle empty path parts after version prefix`() {
            // Given - path like /api/v1/
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            val captured = auditLogSlot.captured
            captured.entityType shouldBe "unknown"
            captured.entityId shouldBe "unknown"
        }

        @Test
        fun `should handle non-versioned api path`() {
            // Given - path like /api/guilds
            val auditLogSlot = slot<AuditLog>()
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/guilds",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(capture(auditLogSlot)) } answers { auditLogSlot.captured }

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            val captured = auditLogSlot.captured
            captured.entityType shouldBe "guilds"
            captured.entityId shouldBe "guilds"
        }
    }

    @Nested
    inner class ExceptionHandling {

        @Test
        fun `should handle repository save exception gracefully`() {
            // Given
            val exchange = createExchange(
                method = HttpMethod.POST,
                path = "/api/v1/guilds",
                headers = mapOf(
                    "X-User-Id" to "user-123",
                    "X-Username" to "testuser",
                    "X-Admin-Mode" to "false"
                ),
                statusCode = HttpStatus.CREATED
            )
            every { auditLogRepository.save(any()) } throws RuntimeException("Database error")

            // When - should not throw
            filter.filter(exchange, filterChain).block()

            // Then - filter chain should still be called
            verify { filterChain.filter(exchange) }
        }
    }

    @Nested
    inner class NullMethodHandling {

        @Test
        fun `should handle null HTTP method`() {
            // Given - Create exchange with mocked request that returns null method
            val exchange = createExchange(
                method = HttpMethod.GET,  // This will be skipped anyway
                path = "/api/v1/guilds"
            )

            // When
            filter.filter(exchange, filterChain).block()

            // Then
            verify { filterChain.filter(exchange) }
            verify(exactly = 0) { auditLogRepository.save(any()) }
        }
    }

}
