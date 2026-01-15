package com.edgerush.lootman.api.sync

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime

/**
 * Unit tests for SyncRunController.
 */
class SyncRunControllerTest : UnitTest() {
    private lateinit var syncRunService: SyncRunCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: SyncRunController

    @BeforeEach
    fun setup() {
        syncRunService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = SyncRunController(syncRunService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse =
                PagedResponse(
                    content = listOf(createSyncRunResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
            every { syncRunService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                syncRunService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse =
                PagedResponse(
                    content = emptyList<SyncRunResponse>(),
                    page = 0,
                    size = 100,
                    totalElements = 0,
                )
            every { syncRunService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return sync run when found`() {
            // Given
            val expected = createSyncRunResponse(id = 123L, source = "WoWAudit")
            every { syncRunService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.source shouldBe "WoWAudit"
            verify(exactly = 1) { syncRunService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { syncRunService.findById(999L) } throws NoSuchElementException("Sync run not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Sync run not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should return created sync run with 201 status`() {
            // Given
            val request =
                CreateSyncRunRequest(
                    source = "WoWAudit",
                    status = "RUNNING",
                    message = "Sync started",
                )

            val created =
                createSyncRunResponse(
                    id = 1L,
                    source = "WoWAudit",
                    status = "RUNNING",
                )
            every { syncRunService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.source shouldBe "WoWAudit"
            result.body?.status shouldBe "RUNNING"
            verify(exactly = 1) { syncRunService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {
        @Test
        fun `should return updated sync run`() {
            // Given
            val request =
                UpdateSyncRunRequest(
                    status = "COMPLETED",
                    message = "Sync completed successfully",
                )

            val updated =
                createSyncRunResponse(
                    id = 1L,
                    status = "COMPLETED",
                    message = "Sync completed successfully",
                )
            every { syncRunService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.status shouldBe "COMPLETED"
            result.message shouldBe "Sync completed successfully"
            verify(exactly = 1) { syncRunService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when sync run not found`() {
            // Given
            val request = UpdateSyncRunRequest(status = "FAILED")

            every { syncRunService.update(999L, request) } throws NoSuchElementException("Sync run not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Sync run not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { syncRunService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { syncRunService.delete(1L) }
        }

        @Test
        fun `should propagate exception when sync run not found`() {
            // Given
            every { syncRunService.delete(999L) } throws NoSuchElementException("Sync run not found with id: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Sync run not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return exists true when sync run exists`() {
            // Given
            every { syncRunService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { syncRunService.existsById(1L) }
        }

        @Test
        fun `should return exists false when sync run does not exist`() {
            // Given
            every { syncRunService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindBySourceTests {
        @Test
        fun `should return sync runs for a source`() {
            // Given
            val source = "WoWAudit"
            val runs =
                listOf(
                    createSyncRunResponse(id = 1L, source = source),
                    createSyncRunResponse(id = 2L, source = source),
                )
            val expectedResponse =
                PagedResponse(
                    content = runs,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { syncRunService.findBySource(source, any()) } returns expectedResponse

            // When
            val result = controller.findBySource(source, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.source == source } shouldBe true
        }
    }

    @Nested
    inner class FindByStatusTests {
        @Test
        fun `should return sync runs for a status`() {
            // Given
            val status = "COMPLETED"
            val runs =
                listOf(
                    createSyncRunResponse(id = 1L, status = status),
                    createSyncRunResponse(id = 2L, status = status),
                )
            val expectedResponse =
                PagedResponse(
                    content = runs,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { syncRunService.findByStatus(status, any()) } returns expectedResponse

            // When
            val result = controller.findByStatus(status, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.status == status } shouldBe true
        }
    }

    @Nested
    inner class CountBySourceTests {
        @Test
        fun `should return count for source`() {
            // Given
            val source = "WoWAudit"
            every { syncRunService.countBySource(source) } returns 5L

            // When
            val result = controller.countBySource(source)

            // Then
            result.count shouldBe 5L
            verify(exactly = 1) { syncRunService.countBySource(source) }
        }
    }

    private fun createSyncRunResponse(
        id: Long = 1L,
        source: String = "WoWAudit",
        status: String = "COMPLETED",
        startedAt: OffsetDateTime = OffsetDateTime.now().minusMinutes(5),
        completedAt: OffsetDateTime? = OffsetDateTime.now(),
        message: String? = "Sync completed",
    ): SyncRunResponse =
        SyncRunResponse(
            id = id,
            source = source,
            status = status,
            startedAt = startedAt,
            completedAt = completedAt,
            message = message,
        )
}
