package com.edgerush.lootman.api.warcraftlogs

import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.api.auth.GuildAccessDeniedException
import com.edgerush.lootman.api.auth.NoLinkedRaiderException
import com.edgerush.lootman.api.raider.RaiderWarcraftLogCrudService
import com.edgerush.lootman.api.raider.RaiderWarcraftLogResponse
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for WarcraftLogsController.
 *
 * Tests the /warcraftlogs endpoints for Warcraft Logs performance data.
 */
class WarcraftLogsControllerTest : UnitTest() {
    private lateinit var warcraftLogService: RaiderWarcraftLogCrudService
    private lateinit var raiderRepository: RaiderEntityRepository
    private lateinit var currentUserService: CurrentUserService
    private lateinit var controller: WarcraftLogsController
    private lateinit var authenticatedUser: AuthenticatedUser

    @BeforeEach
    fun setup() {
        warcraftLogService = mockk()
        raiderRepository = mockk()
        currentUserService = mockk()
        controller = WarcraftLogsController(warcraftLogService, raiderRepository, currentUserService)
        authenticatedUser = mockk()
    }

    // ========== getReports tests ==========

    @Test
    fun `getReports should return reports for specified raider`() {
        // Given
        val guildId = "test-guild"
        val raiderId = 123L
        val limit = 20

        val raider = createRaiderEntity(raiderId, "TestRaider", "Twisting Nether")
        val logs =
            listOf(
                RaiderWarcraftLogResponse(id = 1L, raiderId = raiderId, difficulty = "Mythic", score = 85),
                RaiderWarcraftLogResponse(id = 2L, raiderId = raiderId, difficulty = "Heroic", score = 92),
                RaiderWarcraftLogResponse(id = 3L, raiderId = raiderId, difficulty = "Mythic", score = 78),
            )

        every { raiderRepository.findById(raiderId) } returns raider
        every { warcraftLogService.findByRaiderIdUnpaged(raiderId, limit) } returns logs

        // When
        val response = controller.getReports(guildId, raiderId, limit)

        // Then
        response.raiderId shouldBe raiderId
        response.characterName shouldBe "TestRaider"
        response.reports shouldHaveSize 3
        response.reports[0].reportId shouldStartWith "wcl_"
        response.reports[0].percentile shouldBe 85.0
        response.reports[1].percentile shouldBe 92.0

        verify(exactly = 1) { raiderRepository.findById(raiderId) }
        verify(exactly = 1) { warcraftLogService.findByRaiderIdUnpaged(raiderId, limit) }
    }

    @Test
    fun `getReports should use default limit when not specified`() {
        // Given
        val guildId = "test-guild"
        val raiderId = 123L
        val defaultLimit = 20

        val raider = createRaiderEntity(raiderId, "TestRaider", "Twisting Nether")

        every { raiderRepository.findById(raiderId) } returns raider
        every { warcraftLogService.findByRaiderIdUnpaged(raiderId, defaultLimit) } returns emptyList()

        // When
        val response = controller.getReports(guildId, raiderId, defaultLimit)

        // Then
        response.reports shouldHaveSize 0
        verify(exactly = 1) { warcraftLogService.findByRaiderIdUnpaged(raiderId, defaultLimit) }
    }

    @Test
    fun `getReports should throw exception when raider not found`() {
        // Given
        val guildId = "test-guild"
        val raiderId = 999L

        every { raiderRepository.findById(raiderId) } returns null

        // When / Then
        val exception =
            assertThrows<NoSuchElementException> {
                controller.getReports(guildId, raiderId, 20)
            }

        exception.message shouldBe "Raider not found: $raiderId"
        verify(exactly = 1) { raiderRepository.findById(raiderId) }
        verify(exactly = 0) { warcraftLogService.findByRaiderIdUnpaged(any(), any()) }
    }

    @Test
    fun `getReports should return empty reports when no logs exist`() {
        // Given
        val guildId = "test-guild"
        val raiderId = 123L

        val raider = createRaiderEntity(raiderId, "TestRaider", "Twisting Nether")

        every { raiderRepository.findById(raiderId) } returns raider
        every { warcraftLogService.findByRaiderIdUnpaged(raiderId, 20) } returns emptyList()

        // When
        val response = controller.getReports(guildId, raiderId, 20)

        // Then
        response.raiderId shouldBe raiderId
        response.characterName shouldBe "TestRaider"
        response.reports shouldHaveSize 0
    }

    @Test
    fun `getReports should handle null scores gracefully`() {
        // Given
        val guildId = "test-guild"
        val raiderId = 123L

        val raider = createRaiderEntity(raiderId, "TestRaider", "Twisting Nether")
        val logs =
            listOf(
                RaiderWarcraftLogResponse(id = 1L, raiderId = raiderId, difficulty = "Mythic", score = null),
            )

        every { raiderRepository.findById(raiderId) } returns raider
        every { warcraftLogService.findByRaiderIdUnpaged(raiderId, 20) } returns logs

        // When
        val response = controller.getReports(guildId, raiderId, 20)

        // Then
        response.reports shouldHaveSize 1
        response.reports[0].percentile shouldBe 0.0
    }

    // ========== getMyReports tests ==========

    @Test
    fun `getMyReports should return reports for current user's primary raider`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)
        val limit = 20

        val raider = createRaiderEntity(raiderId.value, "TestRaider", "Twisting Nether")
        val logs =
            listOf(
                RaiderWarcraftLogResponse(id = 1L, raiderId = raiderId.value, difficulty = "Mythic", score = 88),
                RaiderWarcraftLogResponse(id = 2L, raiderId = raiderId.value, difficulty = "Mythic", score = 92),
            )

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { raiderRepository.findById(raiderId.value) } returns raider
        every { warcraftLogService.findByRaiderIdUnpaged(raiderId.value, limit) } returns logs

        // When
        val response = controller.getMyReports(guildId, authenticatedUser, limit)

        // Then
        response.raiderId shouldBe raiderId.value
        response.characterName shouldBe "TestRaider"
        response.reports shouldHaveSize 2
        response.reports[0].percentile shouldBe 88.0

        verify(exactly = 1) { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
    }

    @Test
    fun `getMyReports should throw exception when guild access denied`() {
        // Given
        val guildId = "restricted-guild"

        every { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) } throws
            GuildAccessDeniedException(1L, guildId)

        // When / Then
        assertThrows<GuildAccessDeniedException> {
            controller.getMyReports(guildId, authenticatedUser, 20)
        }

        verify(exactly = 1) { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        verify(exactly = 0) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(any()) }
    }

    @Test
    fun `getMyReports should throw exception when no raider is linked`() {
        // Given
        val guildId = "test-guild"

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } throws
            NoLinkedRaiderException(1L)

        // When / Then
        assertThrows<NoLinkedRaiderException> {
            controller.getMyReports(guildId, authenticatedUser, 20)
        }

        verify(exactly = 1) { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
    }

    @Test
    fun `getMyReports should use custom limit when specified`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)
        val customLimit = 50

        val raider = createRaiderEntity(raiderId.value, "TestRaider", "Twisting Nether")

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { raiderRepository.findById(raiderId.value) } returns raider
        every { warcraftLogService.findByRaiderIdUnpaged(raiderId.value, customLimit) } returns emptyList()

        // When
        val response = controller.getMyReports(guildId, authenticatedUser, customLimit)

        // Then
        response.reports shouldHaveSize 0
        verify(exactly = 1) { warcraftLogService.findByRaiderIdUnpaged(raiderId.value, customLimit) }
    }

    @Test
    fun `getMyReports should map difficulty to encounter name`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)

        val raider = createRaiderEntity(raiderId.value, "TestRaider", "Twisting Nether")
        val logs =
            listOf(
                RaiderWarcraftLogResponse(id = 1L, raiderId = raiderId.value, difficulty = "Mythic", score = 85),
            )

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { raiderRepository.findById(raiderId.value) } returns raider
        every { warcraftLogService.findByRaiderIdUnpaged(raiderId.value, 20) } returns logs

        // When
        val response = controller.getMyReports(guildId, authenticatedUser, 20)

        // Then
        response.reports[0].difficulty shouldBe "Mythic"
        response.reports[0].encounterName shouldBe "Mythic"
    }

    // ========== Helper methods ==========

    private fun createRaiderEntity(
        id: Long,
        characterName: String,
        realm: String,
    ): RaiderEntity {
        return RaiderEntity(
            id = id,
            characterName = characterName,
            realm = realm,
            region = "eu",
            wowauditId = 12345L,
            clazz = "Mage",
            spec = "Frost",
            role = "DPS",
            rank = "Raider",
            status = "Active",
            note = null,
            blizzardId = 12345L,
            trackingSince = java.time.OffsetDateTime.now(),
            joinDate = java.time.OffsetDateTime.now(),
            blizzardLastModified = java.time.OffsetDateTime.now(),
            lastSync = java.time.OffsetDateTime.now(),
        )
    }
}
