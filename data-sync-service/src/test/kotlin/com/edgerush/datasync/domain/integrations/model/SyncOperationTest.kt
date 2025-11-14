package com.edgerush.datasync.domain.integrations.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.Instant

class SyncOperationTest {

    @Test
    fun `should create sync operation with start method`() {
        // When
        val operation = SyncOperation.start(
            source = ExternalDataSource.WOWAUDIT,
            operationType = "roster-sync"
        )

        // Then
        operation.id shouldNotBe null
        operation.source shouldBe ExternalDataSource.WOWAUDIT
        operation.operationType shouldBe "roster-sync"
        operation.status shouldBe SyncStatus.IN_PROGRESS
        operation.startedAt shouldNotBe null
        operation.completedAt shouldBe null
        operation.recordsProcessed shouldBe 0
    }

    @Test
    fun `should complete sync operation with result`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WOWAUDIT,
            operationType = "roster-sync"
        )
        val result = SyncResult.success(
            recordsProcessed = 50,
            startedAt = operation.startedAt,
            message = "Sync completed successfully"
        )

        // When
        val completed = operation.complete(result)

        // Then
        completed.status shouldBe SyncStatus.SUCCESS
        completed.completedAt shouldNotBe null
        completed.recordsProcessed shouldBe 50
        completed.message shouldBe "Sync completed successfully"
        completed.isComplete() shouldBe true
        completed.isSuccess() shouldBe true
    }

    @Test
    fun `should fail sync operation with message`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WOWAUDIT,
            operationType = "roster-sync"
        )

        // When
        val failed = operation.fail(
            message = "API connection failed",
            errors = listOf("Connection timeout", "Retry limit exceeded")
        )

        // Then
        failed.status shouldBe SyncStatus.FAILED
        failed.completedAt shouldNotBe null
        failed.message shouldBe "API connection failed"
        failed.errors.size shouldBe 2
        failed.isComplete() shouldBe true
        failed.isSuccess() shouldBe false
    }

    @Test
    fun `should detect incomplete operation`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WARCRAFT_LOGS,
            operationType = "guild-sync"
        )

        // Then
        operation.isComplete() shouldBe false
    }

    @Test
    fun `should detect completed operation`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WARCRAFT_LOGS,
            operationType = "guild-sync"
        )
        val result = SyncResult.success(
            recordsProcessed = 10,
            startedAt = operation.startedAt
        )

        // When
        val completed = operation.complete(result)

        // Then
        completed.isComplete() shouldBe true
    }
}
