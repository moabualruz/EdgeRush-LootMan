package com.edgerush.lootman.api.team

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
 * Unit tests for TeamMetadataController.
 */
class TeamMetadataControllerTest : UnitTest() {

    private lateinit var teamMetadataService: TeamMetadataCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: TeamMetadataController

    @BeforeEach
    fun setup() {
        teamMetadataService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = TeamMetadataController(teamMetadataService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse = PagedResponse(
                content = listOf(createTeamMetadataResponse(teamId = 1L)),
                page = 0,
                size = 20,
                totalElements = 1,
            )
            every { teamMetadataService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                teamMetadataService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse = PagedResponse(
                content = emptyList<TeamMetadataResponse>(),
                page = 0,
                size = 100,
                totalElements = 0,
            )
            every { teamMetadataService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return team metadata when found`() {
            // Given
            val expected = createTeamMetadataResponse(teamId = 123L, name = "Test Team")
            every { teamMetadataService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.teamId shouldBe 123L
            result.name shouldBe "Test Team"
            verify(exactly = 1) { teamMetadataService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { teamMetadataService.findById(999L) } throws NoSuchElementException("Team metadata not found with teamId: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Team metadata not found with teamId: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {

        @Test
        fun `should return created team metadata with 201 status`() {
            // Given
            val request = CreateTeamMetadataRequest(
                teamId = 1L,
                guildId = 100L,
                guildName = "Edge Rush",
                name = "Main Raid",
                region = "EU",
                realm = "Silvermoon",
                url = "https://wowaudit.com/team/1",
            )

            val created = createTeamMetadataResponse(
                teamId = 1L,
                name = "Main Raid",
                guildName = "Edge Rush",
            )
            every { teamMetadataService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.teamId shouldBe 1L
            result.body?.name shouldBe "Main Raid"
            result.body?.guildName shouldBe "Edge Rush"
            verify(exactly = 1) { teamMetadataService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should return updated team metadata`() {
            // Given
            val request = UpdateTeamMetadataRequest(
                name = "Updated Team Name",
                url = "https://wowaudit.com/team/updated",
            )

            val updated = createTeamMetadataResponse(
                teamId = 1L,
                name = "Updated Team Name",
                url = "https://wowaudit.com/team/updated",
            )
            every { teamMetadataService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.teamId shouldBe 1L
            result.name shouldBe "Updated Team Name"
            result.url shouldBe "https://wowaudit.com/team/updated"
            verify(exactly = 1) { teamMetadataService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when team metadata not found`() {
            // Given
            val request = UpdateTeamMetadataRequest(name = "New Name")

            every { teamMetadataService.update(999L, request) } throws NoSuchElementException("Team metadata not found with teamId: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Team metadata not found with teamId: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { teamMetadataService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { teamMetadataService.delete(1L) }
        }

        @Test
        fun `should propagate exception when team metadata not found`() {
            // Given
            every { teamMetadataService.delete(999L) } throws NoSuchElementException("Team metadata not found with teamId: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Team metadata not found with teamId: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {

        @Test
        fun `should return exists true when team metadata exists`() {
            // Given
            every { teamMetadataService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { teamMetadataService.existsById(1L) }
        }

        @Test
        fun `should return exists false when team metadata does not exist`() {
            // Given
            every { teamMetadataService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByGuildIdTests {

        @Test
        fun `should return team metadata for a guild`() {
            // Given
            val guildId = 100L
            val teams = listOf(
                createTeamMetadataResponse(teamId = 1L, guildId = guildId),
                createTeamMetadataResponse(teamId = 2L, guildId = guildId),
            )
            val expectedResponse = PagedResponse(
                content = teams,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { teamMetadataService.findByGuildId(guildId, any()) } returns expectedResponse

            // When
            val result = controller.findByGuildId(guildId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.guildId == guildId } shouldBe true
        }
    }

    @Nested
    inner class FindByRegionTests {

        @Test
        fun `should return team metadata for a region`() {
            // Given
            val region = "EU"
            val teams = listOf(
                createTeamMetadataResponse(teamId = 1L, region = region),
                createTeamMetadataResponse(teamId = 2L, region = region),
            )
            val expectedResponse = PagedResponse(
                content = teams,
                page = 0,
                size = 20,
                totalElements = 2,
            )
            every { teamMetadataService.findByRegion(region, any()) } returns expectedResponse

            // When
            val result = controller.findByRegion(region, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.region == region } shouldBe true
        }
    }

    @Nested
    inner class CountByGuildIdTests {

        @Test
        fun `should return count for guild`() {
            // Given
            val guildId = 100L
            every { teamMetadataService.countByGuildId(guildId) } returns 3L

            // When
            val result = controller.countByGuildId(guildId)

            // Then
            result.count shouldBe 3L
            verify(exactly = 1) { teamMetadataService.countByGuildId(guildId) }
        }
    }

    private fun createTeamMetadataResponse(
        teamId: Long = 1L,
        guildId: Long? = 100L,
        guildName: String? = "Edge Rush",
        name: String? = "Main Raid",
        region: String? = "EU",
        realm: String? = "Silvermoon",
        url: String? = "https://wowaudit.com/team/1",
        lastRefreshedBlizzard: OffsetDateTime? = OffsetDateTime.now().minusHours(1),
        lastRefreshedPercentiles: OffsetDateTime? = OffsetDateTime.now().minusHours(2),
        lastRefreshedMythicPlus: OffsetDateTime? = OffsetDateTime.now().minusHours(3),
        wishlistUpdatedAt: OffsetDateTime? = OffsetDateTime.now().minusDays(1),
        syncedAt: OffsetDateTime = OffsetDateTime.now(),
    ): TeamMetadataResponse = TeamMetadataResponse(
        teamId = teamId,
        guildId = guildId,
        guildName = guildName,
        name = name,
        region = region,
        realm = realm,
        url = url,
        lastRefreshedBlizzard = lastRefreshedBlizzard,
        lastRefreshedPercentiles = lastRefreshedPercentiles,
        lastRefreshedMythicPlus = lastRefreshedMythicPlus,
        wishlistUpdatedAt = wishlistUpdatedAt,
        syncedAt = syncedAt,
    )
}
