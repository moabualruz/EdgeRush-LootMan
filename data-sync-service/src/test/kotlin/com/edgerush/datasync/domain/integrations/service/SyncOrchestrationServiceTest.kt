package com.edgerush.datasync.domain.integrations.service

import com.edgerush.datasync.domain.integrations.model.ExternalDataSource
import com.edgerush.datasync.domain.integrations.model.SyncOperation
import com.edgerush.datasync.domain.integrations.model.SyncResult
import com.edgerush.datasync.domain.integrations.model.SyncStatus
import com.edgerush.datasync.domain.integrations.repository.SyncOperationRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class SyncOrchestrationServiceTest {

    private lateinit var syncOperationRepository: SyncOperationRepository
    private lateinit var service: SyncOrchestrationService

    @BeforeEach
    fun setup() {
        syncOperationRepository = mockk()
        service = SyncOrchestrationService(syncOperationRepository)
    }

    @Test
    fun `should start sync operation`() {
        // Given
        val operationSlot = slot<SyncOperation>()
        every { syncOperationRepository.save(capture(operationSlot)) } answers { firstArg() }

        // When
        val operation = service.startSync(
            source = ExternalDataSource.WOWAUDIT,
            operationType = "roster-sync"
        )

        // Then
        operation.source shouldBe ExternalDataSource.WOWAUDIT
        operation.operationType shouldBe "roster-sync"
        operation.status shouldBe SyncStatus.IN_PROGRESS
        operation.startedAt shouldNotBe null
        verify(exactly = 1) { syncOperationRepository.save(any()) }
    }

    @Test
    fun `should complete sync operation with success result`() {
        // Given
        val startTime = Instant.now().minusSeconds(10)
        val operation = SyncOperation.start(
            source = ExternalDataSource.WOWAUDIT,
            operationType = "roster-sync"
        )
        val result = SyncResult.success(
            recordsProcessed = 50,
            startedAt = startTime,
            message = "Sync completed"
        )
        every { syncOperationRepository.save(any()) } answers { firstArg() }

        // When
        val completed = service.completeSync(operation, result)

        // Then
        completed.status shouldBe SyncStatus.SUCCESS
        completed.recordsProcessed shouldBe 50
        completed.completedAt shouldNotBe null
        verify(exactly = 1) { syncOperationRepository.save(any()) }
    }

    @Test
    fun `should fail sync operation with error message`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WARCRAFT_LOGS,
            operationType = "guild-sync"
        )
        every { syncOperationRepository.save(any()) } answers { firstArg() }

        // When
        val failed = service.failSync(
            operation = operation,
            message = "API connection failed",
            errors = listOf("Timeout", "Retry exhausted")
        )

        // Then
        failed.status shouldBe SyncStatus.FAILED
        failed.message shouldBe "API connection failed"
        failed.errors.size shouldBe 2
        failed.completedAt shouldNotBe null
        verify(exactly = 1) { syncOperationRepository.save(any()) }
    }

    @Test
    fun `should get latest sync for source`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WOWAUDIT,
            operationType = "roster-sync"
        )
        every { syncOperationRepository.findLatestBySource(ExternalDataSource.WOWAUDIT) } returns operation

        // When
        val latest = service.getLatestSync(ExternalDataSource.WOWAUDIT)

        // Then
        latest shouldNotBe null
        latest?.source shouldBe ExternalDataSource.WOWAUDIT
        verify(exactly = 1) { syncOperationRepository.findLatestBySource(ExternalDataSource.WOWAUDIT) }
    }

    @Test
    fun `should detect sync in progress`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WOWAUDIT,
            operationType = "roster-sync"
        )
        every { syncOperationRepository.findLatestBySource(ExternalDataSource.WOWAUDIT) } returns operation

        // When
        val inProgress = service.isSyncInProgress(ExternalDataSource.WOWAUDIT)

        // Then
        inProgress shouldBe true
    }

    @Test
    fun `should detect no sync in progress when completed`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WOWAUDIT,
            operationType = "roster-sync"
        )
        val result = SyncResult.success(
            recordsProcessed = 10,
            startedAt = operation.startedAt
        )
        val completed = operation.complete(result)
        every { syncOperationRepository.findLatestBySource(ExternalDataSource.WOWAUDIT) } returns completed

        // When
        val inProgress = service.isSyncInProgress(ExternalDataSource.WOWAUDIT)

        // Then
        inProgress shouldBe false
    }

    @Test
    fun `should create success result`() {
        // Given
        val startTime = Instant.now().minusSeconds(5)

        // When
        val result = service.createSuccessResult(
            recordsProcessed = 100,
            startedAt = startTime,
            message = "All good"
        )

        // Then
        result.status shouldBe SyncStatus.SUCCESS
        result.recordsProcessed shouldBe 100
        result.message shouldBe "All good"
    }

    @Test
    fun `should create failure result`() {
        // Given
        val startTime = Instant.now().minusSeconds(5)

        // When
        val result = service.createFailureResult(
            startedAt = startTime,
            message = "Failed",
            errors = listOf("Error 1", "Error 2")
        )

        // Then
        result.status shouldBe SyncStatus.FAILED
        result.message shouldBe "Failed"
        result.errors.size shouldBe 2
    }
}
