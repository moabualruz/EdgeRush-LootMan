package com.edgerush.lootman.domain.audit.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for AuditLog domain model.
 *
 * These tests verify the behavior of the AuditLog aggregate including:
 * - Creation with valid data
 * - Validation of required fields
 * - Factory method behavior
 */
class AuditLogTest : UnitTest() {

    private val now = Instant.now()

    @Nested
    inner class CreationTests {

        @Test
        fun `should create audit log with valid data`() {
            // Given / When
            val auditLog = AuditLog(
                id = AuditLogId(1L),
                timestamp = now,
                operation = AuditOperation.CREATE,
                entityType = "Guild",
                entityId = "guild-123",
                userId = "user-456",
                username = "testuser",
                isAdminMode = false,
                requestId = "req-789"
            )

            // Then
            auditLog.id?.value shouldBe 1L
            auditLog.timestamp shouldBe now
            auditLog.operation shouldBe AuditOperation.CREATE
            auditLog.entityType shouldBe "Guild"
            auditLog.entityId shouldBe "guild-123"
            auditLog.userId shouldBe "user-456"
            auditLog.username shouldBe "testuser"
            auditLog.isAdminMode shouldBe false
            auditLog.requestId shouldBe "req-789"
        }

        @Test
        fun `should create audit log without id for new entries`() {
            // Given / When
            val auditLog = AuditLog(
                id = null,
                timestamp = now,
                operation = AuditOperation.UPDATE,
                entityType = "Raider",
                entityId = "raider-100",
                userId = "admin-1",
                username = "admin",
                isAdminMode = true,
                requestId = null
            )

            // Then
            auditLog.id shouldBe null
            auditLog.operation shouldBe AuditOperation.UPDATE
            auditLog.isAdminMode shouldBe true
            auditLog.requestId shouldBe null
        }

        @Test
        fun `should create audit log with null requestId`() {
            // Given / When
            val auditLog = AuditLog(
                id = AuditLogId(1L),
                timestamp = now,
                operation = AuditOperation.DELETE,
                entityType = "LootAward",
                entityId = "award-123",
                userId = "user-1",
                username = "user",
                isAdminMode = false,
                requestId = null
            )

            // Then
            auditLog.requestId shouldBe null
        }
    }

    @Nested
    inner class FactoryMethodTests {

        @Test
        fun `should create audit log entry using factory method`() {
            // Given / When
            val auditLog = AuditLog.create(
                operation = AuditOperation.CREATE,
                entityType = "Guild",
                entityId = "guild-123",
                userId = "user-456",
                username = "testuser",
                isAdminMode = false,
                requestId = "req-789"
            )

            // Then
            auditLog.id shouldBe null // New entry, no ID yet
            auditLog.timestamp shouldNotBe null
            auditLog.operation shouldBe AuditOperation.CREATE
            auditLog.entityType shouldBe "Guild"
            auditLog.entityId shouldBe "guild-123"
        }

        @Test
        fun `factory method should set current timestamp`() {
            // Given
            val before = Instant.now()

            // When
            val auditLog = AuditLog.create(
                operation = AuditOperation.UPDATE,
                entityType = "Raider",
                entityId = "raider-1",
                userId = "user-1",
                username = "user",
                isAdminMode = false,
                requestId = null
            )

            val after = Instant.now()

            // Then
            auditLog.timestamp.isAfter(before.minusMillis(1)) shouldBe true
            auditLog.timestamp.isBefore(after.plusMillis(1)) shouldBe true
        }

        @Test
        fun `should require non-blank entityType`() {
            // Given / When / Then
            shouldThrow<IllegalArgumentException> {
                AuditLog.create(
                    operation = AuditOperation.CREATE,
                    entityType = "",
                    entityId = "id-123",
                    userId = "user-1",
                    username = "user",
                    isAdminMode = false,
                    requestId = null
                )
            }
        }

        @Test
        fun `should require non-blank entityId`() {
            // Given / When / Then
            shouldThrow<IllegalArgumentException> {
                AuditLog.create(
                    operation = AuditOperation.CREATE,
                    entityType = "Guild",
                    entityId = "",
                    userId = "user-1",
                    username = "user",
                    isAdminMode = false,
                    requestId = null
                )
            }
        }

        @Test
        fun `should require non-blank userId`() {
            // Given / When / Then
            shouldThrow<IllegalArgumentException> {
                AuditLog.create(
                    operation = AuditOperation.CREATE,
                    entityType = "Guild",
                    entityId = "guild-123",
                    userId = "",
                    username = "user",
                    isAdminMode = false,
                    requestId = null
                )
            }
        }

        @Test
        fun `should require non-blank username`() {
            // Given / When / Then
            shouldThrow<IllegalArgumentException> {
                AuditLog.create(
                    operation = AuditOperation.CREATE,
                    entityType = "Guild",
                    entityId = "guild-123",
                    userId = "user-1",
                    username = "   ",
                    isAdminMode = false,
                    requestId = null
                )
            }
        }
    }

    @Nested
    inner class AuditOperationTests {

        @Test
        fun `should have CREATE operation`() {
            AuditOperation.CREATE.name shouldBe "CREATE"
        }

        @Test
        fun `should have UPDATE operation`() {
            AuditOperation.UPDATE.name shouldBe "UPDATE"
        }

        @Test
        fun `should have DELETE operation`() {
            AuditOperation.DELETE.name shouldBe "DELETE"
        }

        @Test
        fun `should have READ operation`() {
            AuditOperation.READ.name shouldBe "READ"
        }
    }

    @Nested
    inner class AuditLogIdTests {

        @Test
        fun `should create audit log id with valid value`() {
            val id = AuditLogId(123L)
            id.value shouldBe 123L
        }

        @Test
        fun `should require positive id value`() {
            shouldThrow<IllegalArgumentException> {
                AuditLogId(0L)
            }
        }

        @Test
        fun `should require non-negative id value`() {
            shouldThrow<IllegalArgumentException> {
                AuditLogId(-1L)
            }
        }
    }
}
