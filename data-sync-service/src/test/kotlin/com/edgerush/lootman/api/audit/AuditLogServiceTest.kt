package com.edgerush.lootman.api.audit

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.audit.model.AuditLog
import com.edgerush.lootman.domain.audit.model.AuditLogId
import com.edgerush.lootman.domain.audit.model.AuditOperation
import com.edgerush.lootman.domain.audit.repository.AuditLogRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class AuditLogServiceTest : UnitTest() {
    private lateinit var auditLogRepository: AuditLogRepository
    private lateinit var service: AuditLogService

    @BeforeEach
    fun setup() {
        auditLogRepository = mockk()
        service = AuditLogService(auditLogRepository)
    }

    @Test
    fun `findByEntity should return audit logs for entity`() {
        // Given
        val entityType = "Raider"
        val entityId = "123"
        val auditLogs =
            listOf(
                createAuditLog(1, entityType, entityId, AuditOperation.CREATE),
                createAuditLog(2, entityType, entityId, AuditOperation.UPDATE),
            )
        every { auditLogRepository.findByEntity(entityType, entityId) } returns auditLogs

        // When
        val result = service.findByEntity(entityType, entityId)

        // Then
        result shouldHaveSize 2
        result[0].id shouldBe 1
        result[0].entityType shouldBe entityType
        result[0].entityId shouldBe entityId
        result[0].operation shouldBe AuditOperation.CREATE
        result[1].id shouldBe 2
        result[1].operation shouldBe AuditOperation.UPDATE
        verify(exactly = 1) { auditLogRepository.findByEntity(entityType, entityId) }
    }

    @Test
    fun `findByEntity should return empty list when no logs exist`() {
        // Given
        every { auditLogRepository.findByEntity(any(), any()) } returns emptyList()

        // When
        val result = service.findByEntity("Guild", "999")

        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `findByUserId should return audit logs for user`() {
        // Given
        val userId = "user-123"
        val auditLogs =
            listOf(
                createAuditLog(1, "Guild", "1", AuditOperation.CREATE, userId = userId),
                createAuditLog(2, "Raider", "5", AuditOperation.DELETE, userId = userId),
            )
        every { auditLogRepository.findByUserId(userId) } returns auditLogs

        // When
        val result = service.findByUserId(userId)

        // Then
        result shouldHaveSize 2
        result.all { it.userId == userId } shouldBe true
        verify(exactly = 1) { auditLogRepository.findByUserId(userId) }
    }

    @Test
    fun `findByTimeRange should return audit logs within range`() {
        // Given
        val from = Instant.parse("2025-01-01T00:00:00Z")
        val to = Instant.parse("2025-01-15T23:59:59Z")
        val auditLogs =
            listOf(
                createAuditLog(1, "Guild", "1", AuditOperation.CREATE, timestamp = Instant.parse("2025-01-05T12:00:00Z")),
                createAuditLog(2, "Raider", "2", AuditOperation.UPDATE, timestamp = Instant.parse("2025-01-10T15:30:00Z")),
            )
        every { auditLogRepository.findByTimeRange(from, to) } returns auditLogs

        // When
        val result = service.findByTimeRange(from, to)

        // Then
        result shouldHaveSize 2
        verify(exactly = 1) { auditLogRepository.findByTimeRange(from, to) }
    }

    @Test
    fun `findByTimeRange should throw when from is after to`() {
        // Given
        val from = Instant.parse("2025-01-15T00:00:00Z")
        val to = Instant.parse("2025-01-01T00:00:00Z")

        // When/Then
        shouldThrow<IllegalArgumentException> {
            service.findByTimeRange(from, to)
        }.message shouldBe "Start time must not be after end time"
    }

    @Test
    fun `findByOperation should return audit logs for operation type`() {
        // Given
        val operation = AuditOperation.DELETE
        val auditLogs =
            listOf(
                createAuditLog(1, "Guild", "1", operation),
                createAuditLog(2, "Raider", "5", operation),
            )
        every { auditLogRepository.findByOperation(operation) } returns auditLogs

        // When
        val result = service.findByOperation(operation)

        // Then
        result shouldHaveSize 2
        result.all { it.operation == operation } shouldBe true
        verify(exactly = 1) { auditLogRepository.findByOperation(operation) }
    }

    @Test
    fun `countByEntity should return count of audit logs`() {
        // Given
        val entityType = "Guild"
        val entityId = "42"
        val auditLogs =
            listOf(
                createAuditLog(1, entityType, entityId, AuditOperation.CREATE),
                createAuditLog(2, entityType, entityId, AuditOperation.UPDATE),
                createAuditLog(3, entityType, entityId, AuditOperation.UPDATE),
            )
        every { auditLogRepository.findByEntity(entityType, entityId) } returns auditLogs

        // When
        val result = service.countByEntity(entityType, entityId)

        // Then
        result shouldBe 3
    }

    @Test
    fun `countByUserId should return count of audit logs for user`() {
        // Given
        val userId = "user-456"
        val auditLogs =
            listOf(
                createAuditLog(1, "Guild", "1", AuditOperation.CREATE, userId = userId),
                createAuditLog(2, "Raider", "2", AuditOperation.UPDATE, userId = userId),
            )
        every { auditLogRepository.findByUserId(userId) } returns auditLogs

        // When
        val result = service.countByUserId(userId)

        // Then
        result shouldBe 2
    }

    private fun createAuditLog(
        id: Long,
        entityType: String,
        entityId: String,
        operation: AuditOperation,
        userId: String = "test-user",
        timestamp: Instant = Instant.now(),
    ): AuditLog {
        return AuditLog(
            id = AuditLogId(id),
            timestamp = timestamp,
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
