package com.edgerush.lootman.api.me

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.api.auth.NoLinkedRaiderException
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

/**
 * Unit tests for MeController.
 *
 * Tests the /me endpoints for current user data retrieval.
 */
class MeControllerTest : UnitTest() {
    private lateinit var currentUserService: CurrentUserService
    private lateinit var meDataService: MeDataService
    private lateinit var controller: MeController
    private lateinit var authenticatedUser: AuthenticatedUser

    @BeforeEach
    fun setup() {
        currentUserService = mockk()
        meDataService = mockk()
        controller = MeController(currentUserService, meDataService)
        authenticatedUser = mockk()
    }

    // ========== getMyGear tests ==========

    @Test
    fun `getMyGear should return gear for current user's primary raider`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)
        val expectedResponse =
            PersonalGearResponse(
                raiderId = 123L,
                raiderName = "TestRaider",
                characterClass = "Mage",
                averageItemLevel = 489.0,
                equippedItemLevel = 489.0,
                items =
                    listOf(
                        GearItemResponse(
                            slot = "HEAD",
                            itemId = 12345L,
                            itemName = "Test Helmet",
                            itemLevel = 489,
                            quality = "EPIC",
                            enchanted = true,
                            gemmed = true,
                            bonusIds = emptyList(),
                        ),
                    ),
                missingEnchants = emptyList(),
                missingGems = emptyList(),
            )

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { meDataService.getGearForRaider(GuildId(guildId), raiderId) } returns expectedResponse

        // When
        val response = controller.getMyGear(guildId, authenticatedUser)

        // Then
        response shouldBe expectedResponse
        response.raiderId shouldBe 123L
        response.raiderName shouldBe "TestRaider"
        response.items.size shouldBe 1

        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
        verify(exactly = 1) { meDataService.getGearForRaider(GuildId(guildId), raiderId) }
    }

    @Test
    fun `getMyGear should throw exception when no raider is linked`() {
        // Given
        val guildId = "test-guild"

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } throws
            NoLinkedRaiderException(1L)

        // When / Then
        assertThrows<NoLinkedRaiderException> {
            controller.getMyGear(guildId, authenticatedUser)
        }

        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
        verify(exactly = 0) { meDataService.getGearForRaider(any(), any()) }
    }

    // ========== getMyVault tests ==========

    @Test
    fun `getMyVault should return vault options for current user's primary raider`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)
        val expectedResponse =
            PersonalVaultResponse(
                raiderId = 123L,
                raiderName = "TestRaider",
                raidSlots =
                    listOf(
                        VaultSlotResponse(slot = 1, unlocked = true, itemLevel = 489, progress = 2, required = 2),
                    ),
                mythicPlusSlots =
                    listOf(
                        VaultSlotResponse(slot = 1, unlocked = true, itemLevel = 486, progress = 1, required = 1),
                    ),
                pvpSlots =
                    listOf(
                        VaultSlotResponse(slot = 1, unlocked = false, itemLevel = null, progress = 500, required = 1250),
                    ),
            )

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { meDataService.getVaultForRaider(GuildId(guildId), raiderId) } returns expectedResponse

        // When
        val response = controller.getMyVault(guildId, authenticatedUser)

        // Then
        response shouldBe expectedResponse
        response.raiderId shouldBe 123L
        response.raidSlots.size shouldBe 1
        response.mythicPlusSlots.size shouldBe 1
        response.pvpSlots.size shouldBe 1

        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
        verify(exactly = 1) { meDataService.getVaultForRaider(GuildId(guildId), raiderId) }
    }

    @Test
    fun `getMyVault should throw exception when no raider is linked`() {
        // Given
        val guildId = "test-guild"

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } throws
            NoLinkedRaiderException(1L)

        // When / Then
        assertThrows<NoLinkedRaiderException> {
            controller.getMyVault(guildId, authenticatedUser)
        }
    }

    // ========== getMyAttendance tests ==========

    @Test
    fun `getMyAttendance should return attendance data for current user's primary raider`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)
        val expectedResponse =
            PersonalAttendanceResponse(
                raiderId = 123L,
                raiderName = "TestRaider",
                overallRate = 0.95,
                currentStreak = 10,
                longestStreak = 15,
                totalRaids = 100,
                attendedRaids = 95,
                acsScore = 0.95,
                breakdown =
                    AttendanceBreakdownResponse(
                        present = 95,
                        late = 2,
                        excused = 1,
                        absent = 2,
                    ),
                recentAttendance =
                    listOf(
                        AttendanceRecordResponse(
                            raidDate = Instant.now(),
                            raidName = "Nerub-ar Palace",
                            status = "PRESENT",
                            note = null,
                        ),
                    ),
            )

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { meDataService.getAttendanceForRaider(GuildId(guildId), raiderId) } returns expectedResponse

        // When
        val response = controller.getMyAttendance(guildId, authenticatedUser)

        // Then
        response shouldBe expectedResponse
        response.raiderId shouldBe 123L
        response.overallRate shouldBe 0.95
        response.attendedRaids shouldBe 95

        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
        verify(exactly = 1) { meDataService.getAttendanceForRaider(GuildId(guildId), raiderId) }
    }

    @Test
    fun `getMyAttendance should throw exception when no raider is linked`() {
        // Given
        val guildId = "test-guild"

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } throws
            NoLinkedRaiderException(1L)

        // When / Then
        assertThrows<NoLinkedRaiderException> {
            controller.getMyAttendance(guildId, authenticatedUser)
        }
    }

    // ========== getMyPerformance tests ==========

    @Test
    fun `getMyPerformance should return performance data for current user's primary raider`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)
        val expectedResponse =
            PersonalPerformanceResponse(
                raiderId = 123L,
                raiderName = "TestRaider",
                characterClass = "Mage",
                spec = "Frost",
                masScore = 0.85,
                averagePerformance = 85.0,
                averageItemLevelPerformance = 82.0,
                killCount = 50,
                bestPerformance = 95.0,
                recentReports =
                    listOf(
                        PerformanceReportResponse(
                            reportId = "abc123",
                            raidName = "Nerub-ar Palace",
                            encounterName = "Ulgrax",
                            date = Instant.now(),
                            percentile = 88.0,
                            ilvlPercentile = 85.0,
                            dps = 150000.0,
                            hps = null,
                        ),
                    ),
                trendData = emptyList(),
            )

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { meDataService.getPerformanceForRaider(GuildId(guildId), raiderId) } returns expectedResponse

        // When
        val response = controller.getMyPerformance(guildId, authenticatedUser)

        // Then
        response shouldBe expectedResponse
        response.raiderId shouldBe 123L
        response.masScore shouldBe 0.85
        response.bestPerformance shouldBe 95.0

        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
        verify(exactly = 1) { meDataService.getPerformanceForRaider(GuildId(guildId), raiderId) }
    }

    @Test
    fun `getMyPerformance should throw exception when no raider is linked`() {
        // Given
        val guildId = "test-guild"

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } throws
            NoLinkedRaiderException(1L)

        // When / Then
        assertThrows<NoLinkedRaiderException> {
            controller.getMyPerformance(guildId, authenticatedUser)
        }
    }

    // ========== getMyWishlist tests ==========

    @Test
    fun `getMyWishlist should return wishlist data for current user's primary raider`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)
        val expectedResponse =
            PersonalWishlistResponse(
                raiderId = 123L,
                raiderName = "TestRaider",
                items =
                    listOf(
                        WishlistItemResponse(
                            itemId = 207788L,
                            itemName = "Fyr'alath the Dreamrender",
                            slot = "MAINHAND",
                            priority = 1,
                            upgradeValue = 8.5,
                            source = "Fyrakk",
                            boss = "Fyrakk",
                            currentItemLevel = 489,
                            wishlistItemLevel = 502,
                            isUpgrade = true,
                        ),
                    ),
                simulationStatus =
                    SimulationStatusResponse(
                        status = "idle",
                        lastRun = Instant.now().minusSeconds(3600),
                        nextScheduled = Instant.now().plusSeconds(3600),
                        isStale = false,
                    ),
            )

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { meDataService.getWishlistForRaider(GuildId(guildId), raiderId) } returns expectedResponse

        // When
        val response = controller.getMyWishlist(guildId, authenticatedUser)

        // Then
        response shouldBe expectedResponse
        response.raiderId shouldBe 123L
        response.items.size shouldBe 1
        response.items[0].upgradeValue shouldBe 8.5
        response.simulationStatus?.isStale shouldBe false

        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
        verify(exactly = 1) { meDataService.getWishlistForRaider(GuildId(guildId), raiderId) }
    }

    @Test
    fun `getMyWishlist should throw exception when no raider is linked`() {
        // Given
        val guildId = "test-guild"

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } throws
            NoLinkedRaiderException(1L)

        // When / Then
        assertThrows<NoLinkedRaiderException> {
            controller.getMyWishlist(guildId, authenticatedUser)
        }
    }

    @Test
    fun `getMyWishlist should return empty items when raider has no wishlist`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)
        val expectedResponse =
            PersonalWishlistResponse(
                raiderId = 123L,
                raiderName = "TestRaider",
                items = emptyList(),
                simulationStatus = null,
            )

        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { meDataService.getWishlistForRaider(GuildId(guildId), raiderId) } returns expectedResponse

        // When
        val response = controller.getMyWishlist(guildId, authenticatedUser)

        // Then
        response.items shouldBe emptyList()
        response.simulationStatus shouldBe null
    }
}
