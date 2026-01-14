package com.edgerush.lootman.api.guild

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
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * Unit tests for GuildConfigurationController.
 */
class GuildConfigurationControllerTest : UnitTest() {

    private lateinit var guildConfigurationService: GuildConfigurationCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: GuildConfigurationController

    @BeforeEach
    fun setup() {
        guildConfigurationService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = GuildConfigurationController(guildConfigurationService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(createGuildConfigurationResponse(id = 1L)),
                page = 0,
                size = 20,
                totalElements = 1,
            )
            every { guildConfigurationService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                guildConfigurationService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse = PagedResponse(
                content = emptyList<GuildConfigurationResponse>(),
                page = 0,
                size = 100,
                totalElements = 0,
            )
            every { guildConfigurationService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return guild configuration when found`() {
            // Given
            val expected = createGuildConfigurationResponse(id = 123L, guildName = "Test Guild")
            every { guildConfigurationService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.guildName shouldBe "Test Guild"
            verify(exactly = 1) { guildConfigurationService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { guildConfigurationService.findById(999L) } throws NoSuchElementException("Guild configuration not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Guild configuration not found with id: 999"
            }
        }
    }

    @Nested
    inner class FindByGuildIdTests {

        @Test
        fun `should return guild configuration when found by guild ID`() {
            // Given
            val guildId = "my-guild"
            val expected = createGuildConfigurationResponse(id = 1L, guildId = guildId)
            every { guildConfigurationService.findByGuildId(guildId) } returns expected

            // When
            val result = controller.findByGuildId(guildId)

            // Then
            result.guildId shouldBe guildId
            verify(exactly = 1) { guildConfigurationService.findByGuildId(guildId) }
        }
    }

    @Nested
    inner class CreateTests {

        @Test
        fun `should return created guild configuration with 201 status`() {
            // Given
            val request = CreateGuildConfigurationRequest(
                guildId = "my-guild",
                guildName = "My Guild",
                guildDescription = "A test guild",
                wowauditGuildUri = "/guilds/123",
                syncEnabled = true,
            )

            val created = createGuildConfigurationResponse(
                id = 1L,
                guildId = "my-guild",
                guildName = "My Guild",
            )
            every { guildConfigurationService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.guildName shouldBe "My Guild"
            verify(exactly = 1) { guildConfigurationService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should return updated guild configuration`() {
            // Given
            val request = UpdateGuildConfigurationRequest(
                guildName = "Updated Guild Name",
                syncEnabled = false,
            )

            val updated = createGuildConfigurationResponse(
                id = 1L,
                guildName = "Updated Guild Name",
                syncEnabled = false,
            )
            every { guildConfigurationService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.guildName shouldBe "Updated Guild Name"
            result.syncEnabled shouldBe false
            verify(exactly = 1) { guildConfigurationService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when guild configuration not found`() {
            // Given
            val request = UpdateGuildConfigurationRequest(syncEnabled = false)

            every { guildConfigurationService.update(999L, request) } throws NoSuchElementException("Guild configuration not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Guild configuration not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { guildConfigurationService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { guildConfigurationService.delete(1L) }
        }
    }

    @Nested
    inner class ExistsTests {

        @Test
        fun `should return exists true when guild configuration exists`() {
            // Given
            every { guildConfigurationService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { guildConfigurationService.existsById(1L) }
        }

        @Test
        fun `should return exists false when guild configuration does not exist`() {
            // Given
            every { guildConfigurationService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindActiveTests {

        @Test
        fun `should return only active guild configurations`() {
            // Given
            val configs = listOf(
                createGuildConfigurationResponse(id = 1L, isActive = true),
                createGuildConfigurationResponse(id = 2L, isActive = true),
            )
            val expectedResponse = PagedResponse(
                content = configs,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { guildConfigurationService.findActive(any()) } returns expectedResponse

            // When
            val result = controller.findActive(page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.isActive } shouldBe true
        }
    }

    @Nested
    inner class UpdateBenchmarkTests {

        @Test
        fun `should update benchmark configuration`() {
            // Given
            val request = UpdateBenchmarkRequest(
                benchmarkMode = "CUSTOM",
                customBenchmarkRms = BigDecimal("0.95"),
                customBenchmarkIpi = BigDecimal("0.90"),
            )

            val updated = createGuildConfigurationResponse(
                id = 1L,
                benchmarkMode = "CUSTOM",
                customBenchmarkRms = BigDecimal("0.95"),
                customBenchmarkIpi = BigDecimal("0.90"),
            )
            every { guildConfigurationService.updateBenchmark(1L, request) } returns updated

            // When
            val result = controller.updateBenchmark(1L, request)

            // Then
            result.benchmarkMode shouldBe "CUSTOM"
            result.customBenchmarkRms shouldBe BigDecimal("0.95")
            verify(exactly = 1) { guildConfigurationService.updateBenchmark(1L, request) }
        }
    }

    @Nested
    inner class UpdateSyncStatusTests {

        @Test
        fun `should update sync status`() {
            // Given
            val guildId = "my-guild"
            val status = "SUCCESS"
            val error: String? = null

            val updated = createGuildConfigurationResponse(
                id = 1L,
                guildId = guildId,
                lastSyncStatus = status,
                lastSyncAt = OffsetDateTime.now(),
            )
            every { guildConfigurationService.updateSyncStatus(guildId, status, error) } returns updated

            // When
            val result = controller.updateSyncStatus(guildId, status, error)

            // Then
            result.lastSyncStatus shouldBe "SUCCESS"
            verify(exactly = 1) { guildConfigurationService.updateSyncStatus(guildId, status, error) }
        }
    }

    private fun createGuildConfigurationResponse(
        id: Long = 1L,
        guildId: String = "test-guild",
        guildName: String = "Test Guild",
        guildDescription: String? = "A test guild",
        wowauditGuildUri: String? = "/guilds/123",
        wowauditBaseUrl: String = "https://wowaudit.com",
        syncEnabled: Boolean = true,
        syncCronExpression: String = "0 0 4 * * *",
        syncRunOnStartup: Boolean = false,
        lastSyncAt: OffsetDateTime? = null,
        lastSyncStatus: String? = null,
        lastSyncError: String? = null,
        timezone: String = "UTC",
        isActive: Boolean = true,
        createdAt: OffsetDateTime = OffsetDateTime.now(),
        updatedAt: OffsetDateTime = OffsetDateTime.now(),
        benchmarkMode: String = "THEORETICAL",
        customBenchmarkRms: BigDecimal? = null,
        customBenchmarkIpi: BigDecimal? = null,
        benchmarkUpdatedAt: OffsetDateTime? = null,
    ): GuildConfigurationResponse = GuildConfigurationResponse(
        id = id,
        guildId = guildId,
        guildName = guildName,
        guildDescription = guildDescription,
        wowauditGuildUri = wowauditGuildUri,
        wowauditBaseUrl = wowauditBaseUrl,
        syncEnabled = syncEnabled,
        syncCronExpression = syncCronExpression,
        syncRunOnStartup = syncRunOnStartup,
        lastSyncAt = lastSyncAt,
        lastSyncStatus = lastSyncStatus,
        lastSyncError = lastSyncError,
        timezone = timezone,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        benchmarkMode = benchmarkMode,
        customBenchmarkRms = customBenchmarkRms,
        customBenchmarkIpi = customBenchmarkIpi,
        benchmarkUpdatedAt = benchmarkUpdatedAt,
    )
}
