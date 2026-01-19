package com.edgerush.lootman.infrastructure.audit

import com.edgerush.datasync.entity.AuditLogEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.audit.model.AuditLog
import com.edgerush.lootman.domain.audit.model.AuditLogId
import com.edgerush.lootman.domain.audit.model.AuditOperation
import com.edgerush.lootman.infrastructure.springdata.AuditLogEntitySpringRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for JdbcAuditLogRepository.
 *
 * These tests mock the Spring Data repository to verify domain/entity mappings.
 * The repository operates on the audit_logs table.
 */
class JdbcAuditLogRepositoryTest : UnitTest() {
    private lateinit var springRepository: AuditLogEntitySpringRepository
    private lateinit var repository: JdbcAuditLogRepository

    private val now = Instant.now()
    private val oneHourAgo = now.minus(1, ChronoUnit.HOURS)
    private val oneDayAgo = now.minus(1, ChronoUnit.DAYS)

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcAuditLogRepository(springRepository)
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save audit log entry and return domain model`() {
            // Given
            val auditLog = createAuditLog(id = null)
            val savedEntity = createAuditLogEntity(id = 1L)

            every { springRepository.save(any()) } returns savedEntity

            // When
            val result = repository.save(auditLog)

            // Then
            result.id?.value shouldBe 1L
            result.operation shouldBe auditLog.operation
            result.entityType shouldBe auditLog.entityType
            result.entityId shouldBe auditLog.entityId

            verify { springRepository.save(any()) }
        }

        @Test
        fun `should map all required fields to entity`() {
            // Given
            val auditLog =
                createAuditLog(
                    id = null,
                    operation = AuditOperation.CREATE,
                    entityType = "Guild",
                    entityId = "guild-123",
                    userId = "user-456",
                    username = "testuser",
                    isAdminMode = true,
                    requestId = "req-789",
                )
            val savedEntity =
                createAuditLogEntity(
                    id = 1L,
                    operation = "CREATE",
                    entityType = "Guild",
                    entityId = "guild-123",
                    userId = "user-456",
                    username = "testuser",
                    isAdminMode = true,
                    requestId = "req-789",
                )

            every { springRepository.save(any()) } returns savedEntity

            // When
            val result = repository.save(auditLog)

            // Then
            result.operation shouldBe AuditOperation.CREATE
            result.entityType shouldBe "Guild"
            result.entityId shouldBe "guild-123"
            result.userId shouldBe "user-456"
            result.username shouldBe "testuser"
            result.isAdminMode shouldBe true
            result.requestId shouldBe "req-789"

            verify {
                springRepository.save(
                    match {
                        it.operation == "CREATE" &&
                            it.entityType == "Guild" &&
                            it.entityId == "guild-123" &&
                            it.userId == "user-456" &&
                            it.username == "testuser" &&
                            it.isAdminMode == true &&
                            it.requestId == "req-789"
                    },
                )
            }
        }

        @Test
        fun `should handle null requestId`() {
            // Given
            val auditLog =
                createAuditLog(
                    id = null,
                    requestId = null,
                )
            val savedEntity = createAuditLogEntity(id = 1L, requestId = null)

            every { springRepository.save(any()) } returns savedEntity

            // When
            val result = repository.save(auditLog)

            // Then
            result.requestId shouldBe null

            verify {
                springRepository.save(match { it.requestId == null })
            }
        }
    }

    @Nested
    inner class FindByEntityTests {
        @Test
        fun `should find audit logs by entity type and id`() {
            // Given
            val entityType = "Guild"
            val entityId = "guild-123"
            val entities =
                listOf(
                    createAuditLogEntity(id = 1L, entityType = entityType, entityId = entityId),
                    createAuditLogEntity(id = 2L, entityType = entityType, entityId = entityId),
                )

            every {
                springRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
            } returns entities

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result.size shouldBe 2
            result.all { it.entityType == entityType } shouldBe true
            result.all { it.entityId == entityId } shouldBe true
        }

        @Test
        fun `should return empty list when no audit logs for entity`() {
            // Given
            val entityType = "Guild"
            val entityId = "non-existent"

            every {
                springRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
            } returns emptyList()

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByUserIdTests {
        @Test
        fun `should find audit logs by user id`() {
            // Given
            val userId = "user-123"
            val entities =
                listOf(
                    createAuditLogEntity(id = 1L, userId = userId),
                    createAuditLogEntity(id = 2L, userId = userId),
                )

            every { springRepository.findByUserIdOrderByTimestampDesc(userId) } returns entities

            // When
            val result = repository.findByUserId(userId)

            // Then
            result.size shouldBe 2
            result.all { it.userId == userId } shouldBe true
        }

        @Test
        fun `should return empty list when user has no audit logs`() {
            // Given
            val userId = "unknown-user"

            every { springRepository.findByUserIdOrderByTimestampDesc(userId) } returns emptyList()

            // When
            val result = repository.findByUserId(userId)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByTimeRangeTests {
        @Test
        fun `should find audit logs within time range`() {
            // Given
            val from = oneDayAgo
            val to = now
            val entities =
                listOf(
                    createAuditLogEntity(id = 1L, timestamp = oneHourAgo),
                    createAuditLogEntity(id = 2L, timestamp = now.minus(30, ChronoUnit.MINUTES)),
                )

            every {
                springRepository.findByTimestampBetweenOrderByTimestampDesc(from, to)
            } returns entities

            // When
            val result = repository.findByTimeRange(from, to)

            // Then
            result.size shouldBe 2
        }

        @Test
        fun `should return empty list when no logs in time range`() {
            // Given
            val from = oneDayAgo.minus(2, ChronoUnit.DAYS)
            val to = oneDayAgo.minus(1, ChronoUnit.DAYS)

            every {
                springRepository.findByTimestampBetweenOrderByTimestampDesc(from, to)
            } returns emptyList()

            // When
            val result = repository.findByTimeRange(from, to)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByOperationTests {
        @Test
        fun `should find audit logs by operation type`() {
            // Given
            val operation = AuditOperation.CREATE
            val entities =
                listOf(
                    createAuditLogEntity(id = 1L, operation = operation.name),
                    createAuditLogEntity(id = 2L, operation = operation.name),
                )

            every { springRepository.findByOperationOrderByTimestampDesc(operation.name) } returns entities

            // When
            val result = repository.findByOperation(operation)

            // Then
            result.size shouldBe 2
            result.all { it.operation == operation } shouldBe true
        }
    }

    @Nested
    inner class DomainEntityMappingTests {
        @Test
        fun `should map all entity fields to domain model`() {
            // Given
            val id = 123L
            val timestamp = Instant.parse("2024-06-01T12:00:00Z")
            val operation = "UPDATE"
            val entityType = "LootAward"
            val entityId = "award-456"
            val userId = "user-789"
            val username = "admin_user"
            val isAdminMode = true
            val requestId = "req-abc"

            val entity =
                createAuditLogEntity(
                    id = id,
                    timestamp = timestamp,
                    operation = operation,
                    entityType = entityType,
                    entityId = entityId,
                    userId = userId,
                    username = username,
                    isAdminMode = isAdminMode,
                    requestId = requestId,
                )

            every {
                springRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
            } returns listOf(entity)

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result.size shouldBe 1
            val auditLog = result.first()
            auditLog.id?.value shouldBe id
            auditLog.timestamp shouldBe timestamp
            auditLog.operation shouldBe AuditOperation.UPDATE
            auditLog.entityType shouldBe entityType
            auditLog.entityId shouldBe entityId
            auditLog.userId shouldBe userId
            auditLog.username shouldBe username
            auditLog.isAdminMode shouldBe isAdminMode
            auditLog.requestId shouldBe requestId
        }

        @Test
        fun `should handle null requestId from entity`() {
            // Given
            val entityType = "Guild"
            val entityId = "guild-123"
            val entity = createAuditLogEntity(id = 1L, requestId = null)

            every {
                springRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
            } returns listOf(entity)

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result.size shouldBe 1
            result.first().requestId shouldBe null
        }

        @Test
        fun `should map all operation types correctly`() {
            // Given
            val entityType = "LootAward"
            val entityId = "award-123"

            val entities =
                listOf(
                    createAuditLogEntity(id = 1L, operation = "UPDATE"),
                    createAuditLogEntity(id = 2L, operation = "DELETE"),
                )

            every {
                springRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
            } returns entities

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result.size shouldBe 2
            result[0].operation shouldBe AuditOperation.UPDATE
            result[1].operation shouldBe AuditOperation.DELETE
        }

        @Test
        fun `should map isAdminMode correctly when true`() {
            // Given
            val entityType = "Guild"
            val entityId = "guild-admin"
            val entity = createAuditLogEntity(id = 1L, isAdminMode = true)

            every {
                springRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
            } returns listOf(entity)

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result.size shouldBe 1
            result.first().isAdminMode shouldBe true
        }

        @Test
        fun `should map isAdminMode correctly when false`() {
            // Given
            val entityType = "Guild"
            val entityId = "guild-user"
            val entity = createAuditLogEntity(id = 1L, isAdminMode = false)

            every {
                springRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
            } returns listOf(entity)

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result.size shouldBe 1
            result.first().isAdminMode shouldBe false
        }
    }

    // Helper methods

    private fun createAuditLogEntity(
        id: Long? = 1L,
        timestamp: Instant = now,
        operation: String = "CREATE",
        entityType: String = "Guild",
        entityId: String = "guild-123",
        userId: String = "user-456",
        username: String = "testuser",
        isAdminMode: Boolean = false,
        requestId: String? = "req-789",
    ): AuditLogEntity =
        AuditLogEntity(
            id = id,
            timestamp = timestamp,
            operation = operation,
            entityType = entityType,
            entityId = entityId,
            userId = userId,
            username = username,
            isAdminMode = isAdminMode,
            requestId = requestId,
        )

    private fun createAuditLog(
        id: AuditLogId? = AuditLogId(1L),
        timestamp: Instant = now,
        operation: AuditOperation = AuditOperation.CREATE,
        entityType: String = "Guild",
        entityId: String = "guild-123",
        userId: String = "user-456",
        username: String = "testuser",
        isAdminMode: Boolean = false,
        requestId: String? = "req-789",
    ): AuditLog =
        AuditLog(
            id = id,
            timestamp = timestamp,
            operation = operation,
            entityType = entityType,
            entityId = entityId,
            userId = userId,
            username = username,
            isAdminMode = isAdminMode,
            requestId = requestId,
        )
}
