package com.edgerush.datasync.application.integrations

import com.edgerush.datasync.domain.integrations.model.ExternalDataSource
import com.edgerush.datasync.domain.integrations.model.SyncOperation
import com.edgerush.datasync.domain.integrations.repository.SyncOperationRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetSyncStatusUseCaseTest {

    private lateinit var syncOperationRepository: SyncOperationRepository
    private lateinit var useCase: GetSyncStatusUseCase

    @BeforeEach
    fun setup() {
        syncOperationRepository = mockk()
        useCase = GetSyncStatusUseCase(syncOperationRepository)
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
        val result = useCase.getLatestForSource(ExternalDataSource.WOWAUDIT)

        // Then
        result shouldNotBe null
        result?.source shouldBe ExternalDataSource.WOWAUDIT
        verify(exactly = 1) { syncOperationRepository.findLatestBySource(ExternalDataSource.WOWAUDIT) }
    }

    @Test
    fun `should return null when no sync exists for source`() {
        // Given
        every { syncOperationRepository.findLatestBySource(ExternalDataSource.RAIDBOTS) } returns null

        // When
        val result = useCase.getLatestForSource(ExternalDataSource.RAIDBOTS)

        // Then
        result shouldBe null
    }

    @Test
    fun `should get recent syncs for source`() {
        // Given
        val operations = listOf(
            SyncOperation.start(ExternalDataSource.WOWAUDIT, "sync-1"),
            SyncOperation.start(ExternalDataSource.WOWAUDIT, "sync-2")
        )
        every { syncOperationRepository.findBySource(ExternalDataSource.WOWAUDIT, 10) } returns operations

        // When
        val result = useCase.getRecentForSource(ExternalDataSource.WOWAUDIT, 10)

        // Then
        result.size shouldBe 2
        verify(exactly = 1) { syncOperationRepository.findBySource(ExternalDataSource.WOWAUDIT, 10) }
    }

    @Test
    fun `should get all recent syncs`() {
        // Given
        val operations = listOf(
            SyncOperation.start(ExternalDataSource.WOWAUDIT, "sync-1"),
            SyncOperation.start(ExternalDataSource.WARCRAFT_LOGS, "sync-2"),
            SyncOperation.start(ExternalDataSource.RAIDBOTS, "sync-3")
        )
        every { syncOperationRepository.findAll(50) } returns operations

        // When
        val result = useCase.getAllRecent(50)

        // Then
        result.size shouldBe 3
        verify(exactly = 1) { syncOperationRepository.findAll(50) }
    }

    @Test
    fun `should detect sync in progress`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WARCRAFT_LOGS,
            operationType = "guild-sync"
        )
        every { syncOperationRepository.findLatestBySource(ExternalDataSource.WARCRAFT_LOGS) } returns operation

        // When
        val inProgress = useCase.isSyncInProgress(ExternalDataSource.WARCRAFT_LOGS)

        // Then
        inProgress shouldBe true
    }

    @Test
    fun `should detect no sync in progress when no operations exist`() {
        // Given
        every { syncOperationRepository.findLatestBySource(ExternalDataSource.RAIDBOTS) } returns null

        // When
        val inProgress = useCase.isSyncInProgress(ExternalDataSource.RAIDBOTS)

        // Then
        inProgress shouldBe false
    }
}
