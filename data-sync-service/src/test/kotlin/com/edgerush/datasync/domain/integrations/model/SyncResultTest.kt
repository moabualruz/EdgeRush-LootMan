package com.edgerush.datasync.domain.integrations.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class SyncResultTest {

    @Test
    fun `should create successful sync result`() {
        // Given
        val startTime = Instant.now().minusSeconds(10)
        val endTime = Instant.now()

        // When
        val result = SyncResult.success(
            recordsProcessed = 100,
            startedAt = startTime,
            completedAt = endTime,
            message = "All records synced"
        )

        // Then
        result.status shouldBe SyncStatus.SUCCESS
        result.recordsProcessed shouldBe 100
        result.message shouldBe "All records synced"
        result.errors.isEmpty() shouldBe true
        result.isSuccess() shouldBe true
        result.isFailed() shouldBe false
        result.duration shouldBe (endTime.toEpochMilli() - startTime.toEpochMilli())
    }

    @Test
    fun `should create failed sync result`() {
        // Given
        val startTime = Instant.now().minusSeconds(5)

        // When
        val result = SyncResult.failed(
            startedAt = startTime,
            message = "Connection failed",
            errors = listOf("Timeout", "Retry exhausted")
        )

        // Then
        result.status shouldBe SyncStatus.FAILED
        result.recordsProcessed shouldBe 0
        result.message shouldBe "Connection failed"
        result.errors.size shouldBe 2
        result.isSuccess() shouldBe false
        result.isFailed() shouldBe true
    }

    @Test
    fun `should create partial success result`() {
        // Given
        val startTime = Instant.now().minusSeconds(15)

        // When
        val result = SyncResult.partialSuccess(
            recordsProcessed = 75,
            startedAt = startTime,
            message = "Some records failed",
            errors = listOf("Record 10 invalid", "Record 25 duplicate")
        )

        // Then
        result.status shouldBe SyncStatus.PARTIAL_SUCCESS
        result.recordsProcessed shouldBe 75
        result.message shouldBe "Some records failed"
        result.errors.size shouldBe 2
        result.isSuccess() shouldBe false
        result.isFailed() shouldBe false
    }

    @Test
    fun `should calculate duration correctly`() {
        // Given
        val startTime = Instant.ofEpochMilli(1000)
        val endTime = Instant.ofEpochMilli(5000)

        // When
        val result = SyncResult.success(
            recordsProcessed = 50,
            startedAt = startTime,
            completedAt = endTime
        )

        // Then
        result.duration shouldBe 4000
    }
}
