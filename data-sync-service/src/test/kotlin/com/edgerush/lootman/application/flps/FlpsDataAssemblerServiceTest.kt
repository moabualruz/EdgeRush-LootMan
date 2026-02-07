package com.edgerush.lootman.application.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.datasync.test.fixtures.RaiderFixtures
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.flps.repository.RaiderPerformanceRepository
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.raider.repository.RaiderCrestCountRepository
import com.edgerush.lootman.domain.raider.repository.RaiderVaultSlotRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.model.ItemQuality
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import com.edgerush.lootman.domain.shared.repository.GearRepository
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import com.edgerush.lootman.domain.shared.repository.WishlistRepository
import com.edgerush.lootman.domain.statistics.repository.RaiderStatisticsRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Unit tests for FlpsDataAssemblerService.
 *
 * Tests the assembly of FLPS calculation data from multiple repositories.
 */
class FlpsDataAssemblerServiceTest : UnitTest() {
    private val raiderRepository = mockk<RaiderRepository>()
    private val attendanceRepository = mockk<AttendanceRepository>()
    private val lootAwardRepository = mockk<LootAwardRepository>()
    private val wishlistRepository = mockk<WishlistRepository>()
    private val gearRepository = mockk<GearRepository>()
    private val lootBanRepository = mockk<LootBanRepository>()
    private val raiderPerformanceRepository = mockk<RaiderPerformanceRepository>()
    private val raiderStatisticsRepository = mockk<RaiderStatisticsRepository>()
    private val raiderVaultSlotRepository = mockk<RaiderVaultSlotRepository>()
    private val raiderCrestCountRepository = mockk<RaiderCrestCountRepository>()

    private val service =
        FlpsDataAssemblerService(
            raiderRepository = raiderRepository,
            attendanceRepository = attendanceRepository,
            lootAwardRepository = lootAwardRepository,
            wishlistRepository = wishlistRepository,
            gearRepository = gearRepository,
            lootBanRepository = lootBanRepository,
            raiderPerformanceRepository = raiderPerformanceRepository,
            raiderStatisticsRepository = raiderStatisticsRepository,
            raiderVaultSlotRepository = raiderVaultSlotRepository,
            raiderCrestCountRepository = raiderCrestCountRepository,
        )

    @Test
    fun `should assemble FLPS data for all raiders in guild`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val raider1 = createRaider(RaiderId(1L), guildId)
        val raider2 = createRaider(RaiderId(2L), guildId)
        val raiders = listOf(raider1, raider2)

        val attendance1 = listOf(createAttendanceRecord(raider1.id, guildId))
        val attendance2 = listOf(createAttendanceRecord(raider2.id, guildId))
        val loot1 = listOf(createLootAward(raider1.id, guildId))
        val loot2 = emptyList<LootAward>()
        val wishlist1 = createWishlist(raider1.id)
        val gear1 = createGearSet()
        val ban2 = listOf(createLootBan(raider2.id, guildId))

        every { raiderRepository.findByGuildId(guildId) } returns raiders
        every { attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(raider1.id, guildId, any(), any()) } returns attendance1
        every { attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(raider2.id, guildId, any(), any()) } returns attendance2
        every { lootAwardRepository.findByRaiderId(raider1.id) } returns loot1
        every { lootAwardRepository.findByRaiderId(raider2.id) } returns loot2
        every { wishlistRepository.findByRaiderId(raider1.id) } returns wishlist1
        every { wishlistRepository.findByRaiderId(raider2.id) } returns null
        every { gearRepository.findCurrentGear(raider1.id) } returns gear1
        every { gearRepository.findCurrentGear(raider2.id) } returns null
        every { lootBanRepository.findActiveByRaiderId(raider1.id, guildId) } returns emptyList()
        every { lootBanRepository.findActiveByRaiderId(raider2.id, guildId) } returns ban2
        every { raiderPerformanceRepository.findByRaiderAndPeriod(any(), any(), any(), any()) } returns null
        every { raiderStatisticsRepository.findByRaiderId(any()) } returns null
        every { raiderVaultSlotRepository.findByRaiderId(any(), any(), any()) } returns emptyList()
        every { raiderCrestCountRepository.findByRaiderId(any(), any(), any()) } returns emptyList()

        // Act
        val result = service.assembleFlpsData(guildId)

        // Assert
        result shouldHaveSize 2

        // Verify first raider's data
        result[0].raider shouldBe raider1
        result[0].attendance shouldBe attendance1
        result[0].lootHistory shouldBe loot1
        result[0].wishlist shouldBe wishlist1
        result[0].gear shouldBe gear1
        result[0].activeBans.shouldBeEmpty()

        // Verify second raider's data
        result[1].raider shouldBe raider2
        result[1].attendance shouldBe attendance2
        result[1].lootHistory shouldBe loot2
        result[1].wishlist shouldBe null
        result[1].gear shouldBe null
        result[1].activeBans shouldBe ban2

        // Verify repository interactions
        verify(exactly = 1) { raiderRepository.findByGuildId(guildId) }
        verify(exactly = 1) { attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(raider1.id, guildId, any(), any()) }
        verify(exactly = 1) { attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(raider2.id, guildId, any(), any()) }
        verify(exactly = 1) { lootAwardRepository.findByRaiderId(raider1.id) }
        verify(exactly = 1) { lootAwardRepository.findByRaiderId(raider2.id) }
    }

    @Test
    fun `should return empty list when guild has no raiders`() {
        // Arrange
        val guildId = GuildId("empty-guild")
        every { raiderRepository.findByGuildId(guildId) } returns emptyList()

        // Act
        val result = service.assembleFlpsData(guildId)

        // Assert
        result.shouldBeEmpty()
        verify(exactly = 1) { raiderRepository.findByGuildId(guildId) }
    }

    @Test
    fun `should use custom lookback days when specified`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val raider = createRaider(RaiderId(1L), guildId)
        val customLookbackDays = 30

        every { raiderRepository.findByGuildId(guildId) } returns listOf(raider)
        every { attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(raider.id, guildId, any(), any()) } returns emptyList()
        every { lootAwardRepository.findByRaiderId(raider.id) } returns emptyList()
        every { wishlistRepository.findByRaiderId(raider.id) } returns null
        every { gearRepository.findCurrentGear(raider.id) } returns null
        every { lootBanRepository.findActiveByRaiderId(raider.id, guildId) } returns emptyList()
        every { raiderPerformanceRepository.findByRaiderAndPeriod(any(), any(), any(), any()) } returns null
        every { raiderStatisticsRepository.findByRaiderId(any()) } returns null
        every { raiderVaultSlotRepository.findByRaiderId(any(), any(), any()) } returns emptyList()
        every { raiderCrestCountRepository.findByRaiderId(any(), any(), any()) } returns emptyList()

        // Act
        val result = service.assembleFlpsData(guildId, customLookbackDays)

        // Assert
        result shouldHaveSize 1
        verify(exactly = 1) {
            attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                raider.id,
                guildId,
                match { startDate ->
                    // Verify that the date range is approximately 30 days
                    val endDate = LocalDate.now()
                    val expectedStartDate = endDate.minusDays(customLookbackDays.toLong())
                    startDate == expectedStartDate
                },
                any(),
            )
        }
    }

    @Test
    fun `should use default lookback of 56 days when not specified`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val raider = createRaider(RaiderId(1L), guildId)

        every { raiderRepository.findByGuildId(guildId) } returns listOf(raider)
        every { attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(raider.id, guildId, any(), any()) } returns emptyList()
        every { lootAwardRepository.findByRaiderId(raider.id) } returns emptyList()
        every { wishlistRepository.findByRaiderId(raider.id) } returns null
        every { gearRepository.findCurrentGear(raider.id) } returns null
        every { lootBanRepository.findActiveByRaiderId(raider.id, guildId) } returns emptyList()
        every { raiderPerformanceRepository.findByRaiderAndPeriod(any(), any(), any(), any()) } returns null
        every { raiderStatisticsRepository.findByRaiderId(any()) } returns null
        every { raiderVaultSlotRepository.findByRaiderId(any(), any(), any()) } returns emptyList()
        every { raiderCrestCountRepository.findByRaiderId(any(), any(), any()) } returns emptyList()

        // Act
        val result = service.assembleFlpsData(guildId)

        // Assert
        result shouldHaveSize 1
        verify(exactly = 1) {
            attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                raider.id,
                guildId,
                match { startDate ->
                    val endDate = LocalDate.now()
                    val expectedStartDate = endDate.minusDays(56)
                    startDate == expectedStartDate
                },
                any(),
            )
        }
    }

    @Test
    fun `should assemble data with all optional fields present`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val raider = createRaider(RaiderId(1L), guildId)
        val attendance = listOf(createAttendanceRecord(raider.id, guildId))
        val loot = listOf(createLootAward(raider.id, guildId))
        val wishlist = createWishlist(raider.id)
        val gear = createGearSet()
        val bans = listOf(createLootBan(raider.id, guildId))

        every { raiderRepository.findByGuildId(guildId) } returns listOf(raider)
        every { attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(raider.id, guildId, any(), any()) } returns attendance
        every { lootAwardRepository.findByRaiderId(raider.id) } returns loot
        every { wishlistRepository.findByRaiderId(raider.id) } returns wishlist
        every { gearRepository.findCurrentGear(raider.id) } returns gear
        every { lootBanRepository.findActiveByRaiderId(raider.id, guildId) } returns bans
        every { raiderPerformanceRepository.findByRaiderAndPeriod(any(), any(), any(), any()) } returns null
        every { raiderStatisticsRepository.findByRaiderId(any()) } returns null
        every { raiderVaultSlotRepository.findByRaiderId(any(), any(), any()) } returns emptyList()
        every { raiderCrestCountRepository.findByRaiderId(any(), any(), any()) } returns emptyList()

        // Act
        val result = service.assembleFlpsData(guildId)

        // Assert
        result shouldHaveSize 1
        val data = result[0]
        data.raider shouldBe raider
        data.attendance shouldHaveSize 1
        data.lootHistory shouldHaveSize 1
        data.wishlist shouldNotBe null
        data.gear shouldNotBe null
        data.activeBans shouldHaveSize 1
    }

    @Test
    fun `should assemble data with all optional fields null`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val raider = createRaider(RaiderId(1L), guildId)

        every { raiderRepository.findByGuildId(guildId) } returns listOf(raider)
        every { attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(raider.id, guildId, any(), any()) } returns emptyList()
        every { lootAwardRepository.findByRaiderId(raider.id) } returns emptyList()
        every { wishlistRepository.findByRaiderId(raider.id) } returns null
        every { gearRepository.findCurrentGear(raider.id) } returns null
        every { lootBanRepository.findActiveByRaiderId(raider.id, guildId) } returns emptyList()
        every { raiderPerformanceRepository.findByRaiderAndPeriod(any(), any(), any(), any()) } returns null
        every { raiderStatisticsRepository.findByRaiderId(any()) } returns null
        every { raiderVaultSlotRepository.findByRaiderId(any(), any(), any()) } returns emptyList()
        every { raiderCrestCountRepository.findByRaiderId(any(), any(), any()) } returns emptyList()

        // Act
        val result = service.assembleFlpsData(guildId)

        // Assert
        result shouldHaveSize 1
        val data = result[0]
        data.raider shouldBe raider
        data.attendance.shouldBeEmpty()
        data.lootHistory.shouldBeEmpty()
        data.wishlist shouldBe null
        data.gear shouldBe null
        data.activeBans.shouldBeEmpty()
    }

    // ===== Helper Functions =====

    private fun createRaider(
        raiderId: RaiderId,
        guildId: GuildId,
    ): Raider =
        RaiderFixtures.createRaider(
            id = raiderId,
            guildId = guildId,
            name = "TestCharacter${raiderId.value}",
            realm = "TestRealm",
            characterClass = CharacterClass.WARRIOR,
            role = Role.DPS,
            rank = "Raider",
            status = RaiderStatus.ACTIVE,
            joinDate = LocalDateTime.now().minusMonths(6),
            wowauditId = 12345L + raiderId.value,
        )

    private fun createAttendanceRecord(
        raiderId: RaiderId,
        guildId: GuildId,
    ): AttendanceRecord =
        AttendanceRecord.create(
            raiderId = raiderId,
            guildId = guildId,
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now(),
            attendedRaids = 8,
            totalRaids = 10,
        )

    private fun createLootAward(
        raiderId: RaiderId,
        guildId: GuildId,
    ): LootAward =
        LootAward(
            id = LootAwardId.generate(),
            itemId = ItemId(12345),
            raiderId = raiderId,
            guildId = guildId,
            awardedAt = Instant.now().minus(7, ChronoUnit.DAYS),
            flpsScore = FlpsScore.of(0.8),
            tier = LootTier.MYTHIC,
        )

    private fun createWishlist(raiderId: RaiderId): Wishlist =
        Wishlist(
            raiderId = raiderId,
            items =
                listOf(
                    WishlistItem(
                        itemId = ItemId(12345),
                        itemName = "Best Upgrade",
                        priority = 1,
                        upgradePercentage = 25.0,
                    ),
                ),
        )

    private fun createGearSet(): GearSet =
        GearSet(
            items =
                mapOf(
                    EquipmentSlot.HEAD to
                        GearItem(
                            itemId = ItemId(1),
                            name = "Tier Helm",
                            itemLevel = 639,
                            quality = ItemQuality.EPIC,
                            slot = EquipmentSlot.HEAD,
                            isTierPiece = true,
                        ),
                ),
            gearSetType = GearSetType.EQUIPPED,
        )

    private fun createLootBan(
        raiderId: RaiderId,
        guildId: GuildId,
    ): LootBan =
        LootBan(
            id = LootBanId.generate(),
            raiderId = raiderId,
            guildId = guildId,
            reason = "Recent loot",
            bannedAt = Instant.now(),
            expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
        )
}
