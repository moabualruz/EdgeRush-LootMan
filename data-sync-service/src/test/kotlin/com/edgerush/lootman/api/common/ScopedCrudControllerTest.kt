package com.edgerush.lootman.api.common

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for GuildScopedCrudController.
 */
class GuildScopedCrudControllerTest : UnitTest() {

    // Test DTOs
    data class TestCreateRequest(val name: String)
    data class TestUpdateRequest(val name: String)
    data class TestResponse(val id: Long, val name: String, val guildId: String)

    private lateinit var mockService: GuildScopedCrudService<Long, TestCreateRequest, TestUpdateRequest, TestResponse>
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: TestGuildScopedController

    inner class TestGuildScopedController(
        service: GuildScopedCrudService<Long, TestCreateRequest, TestUpdateRequest, TestResponse>,
        paginationProperties: PaginationProperties,
    ) : GuildScopedCrudController<Long, TestCreateRequest, TestUpdateRequest, TestResponse>(
        service,
        paginationProperties,
    )

    @BeforeEach
    fun setUp() {
        mockService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = TestGuildScopedController(mockService, paginationProperties)
    }

    @Nested
    inner class FindByGuildTests {

        @Test
        fun `should return entities for guild with pagination`() {
            // Given
            val guildId = "test-guild-123"
            val expectedResponse = PagedResponse(
                content = listOf(
                    TestResponse(1, "Entity 1", guildId),
                    TestResponse(2, "Entity 2", guildId),
                ),
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { mockService.findByGuild(guildId, any()) } returns expectedResponse

            // When
            val result = controller.findByGuild(guildId, page = 0, size = null, paginationProperties)

            // Then
            result shouldBe expectedResponse
            result.content.size shouldBe 2
            result.content.all { it.guildId == guildId } shouldBe true
            verify {
                mockService.findByGuild(guildId, match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should use custom pagination when provided`() {
            // Given
            val guildId = "test-guild-123"
            val expectedResponse = PagedResponse(
                content = emptyList<TestResponse>(),
                page = 2,
                size = 50,
                totalElements = 100,
            )
            every { mockService.findByGuild(guildId, any()) } returns expectedResponse

            // When
            controller.findByGuild(guildId, page = 2, size = 50, paginationProperties)

            // Then
            verify {
                mockService.findByGuild(guildId, match { it.page == 2 && it.size == 50 })
            }
        }

        @Test
        fun `should return empty response when guild has no entities`() {
            // Given
            val guildId = "empty-guild"
            val expectedResponse = PagedResponse<TestResponse>(
                content = emptyList(),
                page = 0,
                size = 20,
                totalElements = 0,
            )
            every { mockService.findByGuild(guildId, any()) } returns expectedResponse

            // When
            val result = controller.findByGuild(guildId, page = 0, size = null, paginationProperties)

            // Then
            result.content shouldBe emptyList()
            result.totalElements shouldBe 0
        }
    }

    @Nested
    inner class CountByGuildTests {

        @Test
        fun `should return count for guild`() {
            // Given
            val guildId = "test-guild-123"
            every { mockService.countByGuild(guildId) } returns 42

            // When
            val result = controller.countByGuild(guildId)

            // Then
            result.count shouldBe 42
            verify { mockService.countByGuild(guildId) }
        }

        @Test
        fun `should return zero count for empty guild`() {
            // Given
            val guildId = "empty-guild"
            every { mockService.countByGuild(guildId) } returns 0

            // When
            val result = controller.countByGuild(guildId)

            // Then
            result.count shouldBe 0
        }
    }
}

/**
 * Unit tests for RaiderScopedCrudController.
 */
class RaiderScopedCrudControllerTest : UnitTest() {

    // Test DTOs
    data class TestCreateRequest(val value: Int)
    data class TestUpdateRequest(val value: Int)
    data class TestResponse(val id: Long, val value: Int, val raiderId: Long)

    private lateinit var mockService: RaiderScopedCrudService<Long, TestCreateRequest, TestUpdateRequest, TestResponse>
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: TestRaiderScopedController

    inner class TestRaiderScopedController(
        service: RaiderScopedCrudService<Long, TestCreateRequest, TestUpdateRequest, TestResponse>,
        paginationProperties: PaginationProperties,
    ) : RaiderScopedCrudController<Long, TestCreateRequest, TestUpdateRequest, TestResponse>(
        service,
        paginationProperties,
    )

    @BeforeEach
    fun setUp() {
        mockService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = TestRaiderScopedController(mockService, paginationProperties)
    }

    @Nested
    inner class FindByRaiderTests {

        @Test
        fun `should return entities for raider with pagination`() {
            // Given
            val raiderId = 123L
            val expectedResponse = PagedResponse(
                content = listOf(
                    TestResponse(1, 100, raiderId),
                    TestResponse(2, 200, raiderId),
                ),
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { mockService.findByRaider(raiderId, any()) } returns expectedResponse

            // When
            val result = controller.findByRaider(raiderId, page = 0, size = null, paginationProperties)

            // Then
            result shouldBe expectedResponse
            result.content.size shouldBe 2
            result.content.all { it.raiderId == raiderId } shouldBe true
            verify {
                mockService.findByRaider(raiderId, match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should use custom pagination when provided`() {
            // Given
            val raiderId = 456L
            val expectedResponse = PagedResponse(
                content = emptyList<TestResponse>(),
                page = 3,
                size = 25,
                totalElements = 75,
            )
            every { mockService.findByRaider(raiderId, any()) } returns expectedResponse

            // When
            controller.findByRaider(raiderId, page = 3, size = 25, paginationProperties)

            // Then
            verify {
                mockService.findByRaider(raiderId, match { it.page == 3 && it.size == 25 })
            }
        }

        @Test
        fun `should return empty response when raider has no entities`() {
            // Given
            val raiderId = 999L
            val expectedResponse = PagedResponse<TestResponse>(
                content = emptyList(),
                page = 0,
                size = 20,
                totalElements = 0,
            )
            every { mockService.findByRaider(raiderId, any()) } returns expectedResponse

            // When
            val result = controller.findByRaider(raiderId, page = 0, size = null, paginationProperties)

            // Then
            result.content shouldBe emptyList()
            result.totalElements shouldBe 0
        }
    }

    @Nested
    inner class CountByRaiderTests {

        @Test
        fun `should return count for raider`() {
            // Given
            val raiderId = 123L
            every { mockService.countByRaider(raiderId) } returns 15

            // When
            val result = controller.countByRaider(raiderId)

            // Then
            result.count shouldBe 15
            verify { mockService.countByRaider(raiderId) }
        }

        @Test
        fun `should return zero count for raider with no entities`() {
            // Given
            val raiderId = 999L
            every { mockService.countByRaider(raiderId) } returns 0

            // When
            val result = controller.countByRaider(raiderId)

            // Then
            result.count shouldBe 0
        }
    }
}
