package com.edgerush.lootman.infrastructure.audit

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.audit.model.AuditLog
import com.edgerush.lootman.domain.audit.model.AuditLogId
import com.edgerush.lootman.domain.audit.model.AuditOperation
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for JdbcAuditLogRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the audit_logs table.
 */
class JdbcAuditLogRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcAuditLogRepository

    private val now = Instant.now()
    private val oneHourAgo = now.minus(1, ChronoUnit.HOURS)
    private val oneDayAgo = now.minus(1, ChronoUnit.DAYS)

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcAuditLogRepository(jdbcTemplate)
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert audit log entry`() {
            // Given
            val auditLog = createAuditLog(id = null)
            val sqlSlot = slot<String>()

            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(auditLog)

            // Then
            result shouldBe auditLog
            sqlSlot.captured.contains("INSERT INTO") shouldBe true
            sqlSlot.captured.contains("audit_logs") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") && it.contains("audit_logs") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should insert with all required fields`() {
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

            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            repository.save(auditLog)

            // Then
            verify {
                jdbcTemplate.update(
                    any<String>(),
                    any<Timestamp>(), // timestamp
                    eq("CREATE"), // operation
                    eq("Guild"), // entity_type
                    eq("guild-123"), // entity_id
                    eq("user-456"), // user_id
                    eq("testuser"), // username
                    eq(true), // is_admin_mode
                    eq("req-789"), // request_id
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

            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            repository.save(auditLog)

            // Then
            verify {
                jdbcTemplate.update(
                    any<String>(),
                    any<Timestamp>(),
                    any<String>(),
                    any<String>(),
                    any<String>(),
                    any<String>(),
                    any<String>(),
                    any<Boolean>(),
                    isNull(), // request_id should be null
                )
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

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("entity_type = ?") &&
                            it.contains("entity_id = ?")
                    },
                    any<RowMapper<AuditLog>>(),
                    eq(entityType),
                    eq(entityId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AuditLog>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, entityType = entityType, entityId = entityId), 0),
                    rowMapper.mapRow(mockResultSet(2L, entityType = entityType, entityId = entityId), 1),
                )
            }

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
                jdbcTemplate.query(
                    match<String> { it.contains("entity_type = ?") && it.contains("entity_id = ?") },
                    any<RowMapper<AuditLog>>(),
                    eq(entityType),
                    eq(entityId),
                )
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

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("user_id = ?") },
                    any<RowMapper<AuditLog>>(),
                    eq(userId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AuditLog>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, userId = userId), 0),
                    rowMapper.mapRow(mockResultSet(2L, userId = userId), 1),
                )
            }

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

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("user_id = ?") },
                    any<RowMapper<AuditLog>>(),
                    eq(userId),
                )
            } returns emptyList()

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

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("timestamp >= ?") &&
                            it.contains("timestamp <= ?")
                    },
                    any<RowMapper<AuditLog>>(),
                    any<Timestamp>(),
                    any<Timestamp>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AuditLog>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, timestamp = oneHourAgo), 0),
                    rowMapper.mapRow(mockResultSet(2L, timestamp = now.minus(30, ChronoUnit.MINUTES)), 1),
                )
            }

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
                jdbcTemplate.query(
                    match<String> { it.contains("timestamp >= ?") && it.contains("timestamp <= ?") },
                    any<RowMapper<AuditLog>>(),
                    any<Timestamp>(),
                    any<Timestamp>(),
                )
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

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("operation = ?") },
                    any<RowMapper<AuditLog>>(),
                    eq(operation.name),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AuditLog>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, operation = operation), 0),
                    rowMapper.mapRow(mockResultSet(2L, operation = operation), 1),
                )
            }

            // When
            val result = repository.findByOperation(operation)

            // Then
            result.size shouldBe 2
            result.all { it.operation == operation } shouldBe true
        }
    }

    @Nested
    inner class RowMapperTests {
        @Test
        fun `should map all database fields to domain model`() {
            // Given
            val id = 123L
            val timestamp = Instant.parse("2024-06-01T12:00:00Z")
            val operation = AuditOperation.UPDATE
            val entityType = "LootAward"
            val entityId = "award-456"
            val userId = "user-789"
            val username = "admin_user"
            val isAdminMode = true
            val requestId = "req-abc"

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("entity_type = ?") && it.contains("entity_id = ?") },
                    any<RowMapper<AuditLog>>(),
                    eq(entityType),
                    eq(entityId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AuditLog>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = id,
                            timestamp = timestamp,
                            operation = operation,
                            entityType = entityType,
                            entityId = entityId,
                            userId = userId,
                            username = username,
                            isAdminMode = isAdminMode,
                            requestId = requestId,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result.size shouldBe 1
            val auditLog = result.first()
            auditLog.id?.value shouldBe id
            auditLog.timestamp shouldBe timestamp
            auditLog.operation shouldBe operation
            auditLog.entityType shouldBe entityType
            auditLog.entityId shouldBe entityId
            auditLog.userId shouldBe userId
            auditLog.username shouldBe username
            auditLog.isAdminMode shouldBe isAdminMode
            auditLog.requestId shouldBe requestId
        }

        @Test
        fun `should handle null requestId from database`() {
            // Given
            val entityType = "Guild"
            val entityId = "guild-123"

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("entity_type = ?") && it.contains("entity_id = ?") },
                    any<RowMapper<AuditLog>>(),
                    eq(entityType),
                    eq(entityId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AuditLog>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, requestId = null), 0),
                )
            }

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result.size shouldBe 1
            result.first().requestId shouldBe null
        }

        @Test
        fun `should map all operation types correctly`() {
            // Given - Test different AuditOperation enum values
            val entityType = "LootAward"
            val entityId = "award-123"

            // Test UPDATE operation
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("entity_type = ?") && it.contains("entity_id = ?") },
                    any<RowMapper<AuditLog>>(),
                    eq(entityType),
                    eq(entityId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AuditLog>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, operation = AuditOperation.UPDATE), 0),
                    rowMapper.mapRow(mockResultSet(2L, operation = AuditOperation.DELETE), 0),
                )
            }

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

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("entity_type = ?") && it.contains("entity_id = ?") },
                    any<RowMapper<AuditLog>>(),
                    eq(entityType),
                    eq(entityId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AuditLog>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, isAdminMode = true), 0),
                )
            }

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

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("entity_type = ?") && it.contains("entity_id = ?") },
                    any<RowMapper<AuditLog>>(),
                    eq(entityType),
                    eq(entityId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<AuditLog>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, isAdminMode = false), 0),
                )
            }

            // When
            val result = repository.findByEntity(entityType, entityId)

            // Then
            result.size shouldBe 1
            result.first().isAdminMode shouldBe false
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: Long,
        timestamp: Instant = now,
        operation: AuditOperation = AuditOperation.CREATE,
        entityType: String = "Guild",
        entityId: String = "guild-123",
        userId: String = "user-456",
        username: String = "testuser",
        isAdminMode: Boolean = false,
        requestId: String? = "req-789",
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getTimestamp("timestamp") } returns Timestamp.from(timestamp)
        every { rs.getString("operation") } returns operation.name
        every { rs.getString("entity_type") } returns entityType
        every { rs.getString("entity_id") } returns entityId
        every { rs.getString("user_id") } returns userId
        every { rs.getString("username") } returns username
        every { rs.getBoolean("is_admin_mode") } returns isAdminMode
        every { rs.getString("request_id") } returns requestId
        return rs
    }

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
