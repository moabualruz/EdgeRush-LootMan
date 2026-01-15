package com.edgerush.lootman.api.audit

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.audit.model.AuditOperation
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class AuditLogControllerTest : UnitTest() {

    private lateinit var auditLogService: AuditLogService
    private lateinit var controller: AuditLogController

    @BeforeEach
    fun setup() {
        auditLogService = mockk()
        controller = AuditLogController(auditLogService)
    }

    @Test
    fun `findByEntity should return audit logs for entity`() {
        // Given
        val entityType = "Raider"
        val entityId = "123"
        val responses = listOf(
            createAuditLogResponse(1, entityType, entityId, AuditOperation.CREATE),
            createAuditLogResponse(2, entityType, entityId, AuditOperation.UPDATE),
        )
        every { auditLogService.findByEntity(entityType, entityId) } returns responses

        // When
        val result = controller.findByEntity(entityType, entityId)

        // Then
        result shouldHaveSize 2
        result[0].entityType shouldBe entityType
        result[0].entityId shouldBe entityId
        verify(exactly = 1) { auditLogService.findByEntity(entityType, entityId) }
    }

    @Test
    fun `findByUserId should return audit logs for user`() {
        // Given
        val userId = "user-123"
        val responses = listOf(
            createAuditLogResponse(1, "Guild", "1", AuditOperation.CREATE, userId = userId),
        )
        every { auditLogService.findByUserId(userId) } returns responses

        // When
        val result = controller.findByUserId(userId)

        // Then
        result shouldHaveSize 1
        result[0].userId shouldBe userId
        verify(exactly = 1) { auditLogService.findByUserId(userId) }
    }

    @Test
    fun `findByTimeRange should return audit logs within range`() {
        // Given
        val from = Instant.parse("2025-01-01T00:00:00Z")
        val to = Instant.parse("2025-01-15T23:59:59Z")
        val responses = listOf(
            createAuditLogResponse(1, "Guild", "1", AuditOperation.CREATE),
            createAuditLogResponse(2, "Raider", "2", AuditOperation.UPDATE),
        )
        every { auditLogService.findByTimeRange(from, to) } returns responses

        // When
        val result = controller.findByTimeRange(from, to)

        // Then
        result shouldHaveSize 2
        verify(exactly = 1) { auditLogService.findByTimeRange(from, to) }
    }

    @Test
    fun `findByOperation should return audit logs for operation type`() {
        // Given
        val operation = AuditOperation.DELETE
        val responses = listOf(
            createAuditLogResponse(1, "Guild", "1", operation),
        )
        every { auditLogService.findByOperation(operation) } returns responses

        // When
        val result = controller.findByOperation(operation)

        // Then
        result shouldHaveSize 1
        result[0].operation shouldBe operation
        verify(exactly = 1) { auditLogService.findByOperation(operation) }
    }

    @Test
    fun `countByEntity should return count response`() {
        // Given
        val entityType = "Guild"
        val entityId = "42"
        every { auditLogService.countByEntity(entityType, entityId) } returns 5

        // When
        val result = controller.countByEntity(entityType, entityId)

        // Then
        result.count shouldBe 5
        verify(exactly = 1) { auditLogService.countByEntity(entityType, entityId) }
    }

    @Test
    fun `countByUserId should return count response`() {
        // Given
        val userId = "user-456"
        every { auditLogService.countByUserId(userId) } returns 3

        // When
        val result = controller.countByUserId(userId)

        // Then
        result.count shouldBe 3
        verify(exactly = 1) { auditLogService.countByUserId(userId) }
    }

    private fun createAuditLogResponse(
        id: Long,
        entityType: String,
        entityId: String,
        operation: AuditOperation,
        userId: String = "test-user",
    ): AuditLogResponse {
        return AuditLogResponse(
            id = id,
            timestamp = Instant.now(),
            operation = operation,
            entityType = entityType,
            entityId = entityId,
            userId = userId,
            username = "Test User",
            isAdminMode = false,
            requestId = "req-$id",
        )
    }
}
