package com.edgerush.datasync.api.v1

import com.edgerush.datasync.domain.integrations.model.ExternalDataSource
import com.edgerush.datasync.domain.integrations.model.SyncOperation
import com.edgerush.datasync.domain.integrations.repository.SyncOperationRepository
import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

class IntegrationControllerIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var syncOperationRepository: SyncOperationRepository

    @Test
    fun `should get sync status for source`() {
        // Given
        val operation = SyncOperation.start(
            source = ExternalDataSource.WOWAUDIT,
            operationType = "test-sync"
        )
        syncOperationRepository.save(operation)

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/integrations/status/WOWAUDIT",
            Map::class.java
        )

        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
    }

    @Test
    fun `should return 404 when no sync exists for source`() {
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/integrations/status/RAIDBOTS",
            Map::class.java
        )

        // Then
        response.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    @Test
    fun `should return 400 for invalid data source`() {
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/integrations/status/INVALID_SOURCE",
            Map::class.java
        )

        // Then
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
    }

    @Test
    fun `should get recent sync operations for source`() {
        // Given
        val operation1 = SyncOperation.start(ExternalDataSource.WARCRAFT_LOGS, "sync-1")
        val operation2 = SyncOperation.start(ExternalDataSource.WARCRAFT_LOGS, "sync-2")
        syncOperationRepository.save(operation1)
        syncOperationRepository.save(operation2)

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/integrations/status/WARCRAFT_LOGS/recent?limit=10",
            List::class.java
        )

        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
    }

    @Test
    fun `should get all recent sync operations`() {
        // Given
        syncOperationRepository.save(SyncOperation.start(ExternalDataSource.WOWAUDIT, "sync-1"))
        syncOperationRepository.save(SyncOperation.start(ExternalDataSource.WARCRAFT_LOGS, "sync-2"))

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/integrations/status/all?limit=50",
            List::class.java
        )

        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
    }

    @Test
    fun `should check if sync is in progress`() {
        // Given
        val operation = SyncOperation.start(ExternalDataSource.WOWAUDIT, "test-sync")
        syncOperationRepository.save(operation)

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/integrations/status/WOWAUDIT/in-progress",
            Map::class.java
        )

        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!["inProgress"] shouldBe true
    }
}
