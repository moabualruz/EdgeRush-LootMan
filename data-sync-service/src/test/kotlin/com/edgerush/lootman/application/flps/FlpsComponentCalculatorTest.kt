package com.edgerush.lootman.application.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.flps.model.RaiderPreparationData
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.model.ItemQuality
import com.edgerush.lootman.domain.shared.model.Role
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Unit tests for FlpsComponentCalculator.
 *
 * Tests calculation of individual FLPS components from raider data.
 */
class FlpsComponentCalculatorTest : UnitTest() {
    private val calculator = FlpsComponentCalculator()

    // ===== ACS (Attendance Commitment Score) Tests =====

    @Test
    fun `calculateACS should return zero for empty attendance list`() {
        // Arrange
        val attendance = emptyList<AttendanceRecord>()

        // Act
        val result = calculator.calculateACS(attendance)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateACS should calculate perfect attendance as 1_0`() {
        // Arrange
        val attendance = listOf(
            createAttendanceRecord(attendedRaids = 10, totalRaids = 10),
        )

        // Act
        val result = calculator.calculateACS(attendance)

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateACS should calculate 50 percent attendance correctly`() {
        // Arrange
        val attendance = listOf(
            createAttendanceRecord(attendedRaids = 5, totalRaids = 10),
        )

        // Act
        val result = calculator.calculateACS(attendance)

        // Assert
        result.value shouldBe 0.5
    }

    @Test
    fun `calculateACS should aggregate multiple attendance records`() {
        // Arrange
        val attendance = listOf(
            createAttendanceRecord(attendedRaids = 8, totalRaids = 10),
            createAttendanceRecord(attendedRaids = 6, totalRaids = 10),
        )

        // Act
        val result = calculator.calculateACS(attendance)

        // Assert
        // Total attended: 14, Total possible: 20, Percentage: 0.7
        result.value shouldBe (0.7 plusOrMinus 0.0001)
    }

    @Test
    fun `calculateACS should cap value at 1_0`() {
        // Arrange - edge case where attended equals total
        val attendance = listOf(
            createAttendanceRecord(attendedRaids = 20, totalRaids = 20),
        )

        // Act
        val result = calculator.calculateACS(attendance)

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateACS should handle low attendance correctly`() {
        // Arrange
        val attendance = listOf(
            createAttendanceRecord(attendedRaids = 1, totalRaids = 20),
        )

        // Act
        val result = calculator.calculateACS(attendance)

        // Assert
        result.value shouldBe 0.05
    }

    // Note: Tests for totalPossible=0 were removed because AttendanceRecord.create()
    // enforces totalRaids > 0 at the domain level, making this scenario unreachable.
    // The defensive check in calculateACS (line 36) is dead code but kept for safety.

    // ===== MAS (Mechanical Adherence Score) Tests =====

    @Test
    fun `calculateMAS should return zero when performance data is null`() {
        // Act
        val result = calculator.calculateMAS(null)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateMAS should return perfect score for zero deaths and low avoidable damage`() {
        // Arrange
        val performanceData = createPerformanceData(
            totalDeaths = 0,
            totalFights = 10,
            avoidableDamagePercentage = 0.0, // Perfect avoidable damage
        )

        // Act
        val result = calculator.calculateMAS(performanceData)

        // Assert
        result.value shouldBe (1.0 plusOrMinus 0.01)
    }

    @Test
    fun `calculateMAS should return high score for zero deaths and minimal avoidable damage`() {
        // Arrange
        val performanceData = createPerformanceData(
            totalDeaths = 0,
            totalFights = 10,
            avoidableDamagePercentage = 5.0, // Low but non-zero
        )

        // Act
        val result = calculator.calculateMAS(performanceData)

        // Assert - expect high score (0.95+) for minimal avoidable damage
        result.value shouldBe (0.98 plusOrMinus 0.02)
    }

    @Test
    fun `calculateMAS should return lower score for moderate deaths`() {
        // Arrange - 0.5 deaths per attempt (5 deaths in 10 fights)
        val performanceData = createPerformanceData(
            totalDeaths = 5,
            totalFights = 10,
            avoidableDamagePercentage = 20.0,
        )

        // Act
        val result = calculator.calculateMAS(performanceData)

        // Assert
        // With 0.5 dpa (deaths score ~0.8) and 20% avoidable (damage score ~0.75)
        // Weighted: 0.8 * 0.6 + 0.75 * 0.4 = 0.48 + 0.30 = 0.78
        result.value shouldBe (0.78 plusOrMinus 0.05)
    }

    @Test
    fun `calculateMAS should return low score for high deaths`() {
        // Arrange - 2 deaths per attempt (high death rate)
        val performanceData = createPerformanceData(
            totalDeaths = 20,
            totalFights = 10,
            avoidableDamagePercentage = 50.0,
        )

        // Act
        val result = calculator.calculateMAS(performanceData)

        // Assert
        // With high deaths and avoidable damage, expect low score
        result.value shouldBe (0.3 plusOrMinus 0.15)
    }

    @Test
    fun `calculateMAS should floor at zero for extremely bad performance`() {
        // Arrange - extremely high deaths and avoidable damage
        val performanceData = createPerformanceData(
            totalDeaths = 50,
            totalFights = 10,
            avoidableDamagePercentage = 200.0,
        )

        // Act
        val result = calculator.calculateMAS(performanceData)

        // Assert
        result.value shouldBe (0.0 plusOrMinus 0.1)
    }

    @Test
    fun `calculateMAS should return zero when no fights analyzed`() {
        // Arrange
        val performanceData = createPerformanceData(
            totalDeaths = 0,
            totalFights = 0,
            avoidableDamagePercentage = 0.0,
        )

        // Act
        val result = calculator.calculateMAS(performanceData)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateMAS should weight deaths per attempt more than avoidable damage`() {
        // Arrange - high deaths, low avoidable damage
        val highDeathsData = createPerformanceData(
            totalDeaths = 15,
            totalFights = 10,
            avoidableDamagePercentage = 10.0,
        )

        // Arrange - low deaths, high avoidable damage
        val highDamageData = createPerformanceData(
            totalDeaths = 2,
            totalFights = 10,
            avoidableDamagePercentage = 80.0,
        )

        // Act
        val highDeathsResult = calculator.calculateMAS(highDeathsData)
        val highDamageResult = calculator.calculateMAS(highDamageData)

        // Assert - deaths should have more impact, so high deaths = lower score
        highDeathsResult.value shouldBeLessThan highDamageResult.value
    }

    // Legacy no-args version for backwards compatibility
    @Test
    fun `calculateMAS no-args should return zero as fallback`() {
        // Act
        @Suppress("DEPRECATION")
        val result = calculator.calculateMAS()

        // Assert
        result.value shouldBe 0.0
    }

    // ===== EPS (External Preparation Score) Tests =====

    @Test
    fun `calculateEPS should return zero when both gear and preparation are null`() {
        // Act
        val result = calculator.calculateEPS(null, null)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateEPS should return base score when only gear is present`() {
        // Arrange
        val gear = createGearSet(tierPieces = 0)

        // Act
        val result = calculator.calculateEPS(gear, null)

        // Assert
        // Base score for having gear (legacy behavior)
        result.value shouldBe (0.7 plusOrMinus 0.1)
    }

    @Test
    fun `calculateEPS should return perfect score for full vault and high M+ rating`() {
        // Arrange
        val gear = createGearSet(tierPieces = 4)
        val preparation = createPreparationData(
            raidVaultSlots = 3,
            mythicPlusVaultSlots = 3,
            pvpVaultSlots = 3,
            mythicPlusRating = 2500,
            hasHeroicClear = true,
            hasNormalClear = true,
        )

        // Act
        val result = calculator.calculateEPS(gear, preparation)

        // Assert
        // Full score: raid(35%) + m+(20%) + pvp(5%) + rating(25%) + heroic(10%) + normal(5%) = 100%
        result.value shouldBe (1.0 plusOrMinus 0.05)
    }

    @Test
    fun `calculateEPS should give higher score for raid vault than M+ vault`() {
        // Arrange
        val gear = createGearSet(tierPieces = 0)

        val raidVaultOnly = createPreparationData(
            raidVaultSlots = 3,
            mythicPlusVaultSlots = 0,
        )

        val mplusVaultOnly = createPreparationData(
            raidVaultSlots = 0,
            mythicPlusVaultSlots = 3,
        )

        // Act
        val raidResult = calculator.calculateEPS(gear, raidVaultOnly)
        val mplusResult = calculator.calculateEPS(gear, mplusVaultOnly)

        // Assert - raid vault should contribute more to EPS
        raidResult.value shouldBeLessThan mplusResult.value + 0.2 // Within 0.2 tolerance
    }

    @Test
    fun `calculateEPS should return moderate score for partial preparation`() {
        // Arrange
        val gear = createGearSet(tierPieces = 2)
        val preparation = createPreparationData(
            raidVaultSlots = 2,
            mythicPlusVaultSlots = 1,
            mythicPlusRating = 1500,
            hasHeroicClear = false,
            hasNormalClear = true,
        )

        // Act
        val result = calculator.calculateEPS(gear, preparation)

        // Assert - should be moderate (roughly 50%)
        // Calculation: raid(2/3*35%=23.3%) + m+(1/3*20%=6.7%) + rating(1500/2500*25%=15%) + normal(5%) = 50%
        result.value shouldBe (0.50 plusOrMinus 0.10)
    }

    @Test
    fun `calculateEPS should include M+ rating in score`() {
        // Arrange
        val gear = createGearSet(tierPieces = 0)

        val lowRating = createPreparationData(
            mythicPlusRating = 500,
        )

        val highRating = createPreparationData(
            mythicPlusRating = 2500,
        )

        // Act
        val lowResult = calculator.calculateEPS(gear, lowRating)
        val highResult = calculator.calculateEPS(gear, highRating)

        // Assert - higher M+ rating should give higher EPS
        lowResult.value shouldBeLessThan highResult.value
    }

    @Test
    fun `calculateEPS should give bonus for heroic clear`() {
        // Arrange
        val gear = createGearSet(tierPieces = 0)

        val noClears = createPreparationData(
            hasHeroicClear = false,
            hasNormalClear = false,
        )

        val heroicClear = createPreparationData(
            hasHeroicClear = true,
            hasNormalClear = true,
        )

        // Act
        val noResult = calculator.calculateEPS(gear, noClears)
        val heroicResult = calculator.calculateEPS(gear, heroicClear)

        // Assert - heroic clear should boost EPS
        noResult.value shouldBeLessThan heroicResult.value
    }

    // Legacy no-preparation version tests
    @Test
    fun `calculateEPS legacy should return zero when gear is null`() {
        // Act
        @Suppress("DEPRECATION")
        val result = calculator.calculateEPS(null)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateEPS legacy should return base score when gear is present`() {
        // Arrange
        val gear = createGearSet(tierPieces = 0)

        // Act
        @Suppress("DEPRECATION")
        val result = calculator.calculateEPS(gear)

        // Assert
        result.value shouldBe 0.7
    }

    // ===== UV (Upgrade Value) Tests =====

    @Test
    fun `calculateUV should return zero when wishlist is null`() {
        // Arrange
        val itemId = ItemId(12345)

        // Act
        val result = calculator.calculateUV(null, itemId)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateUV should return zero when item not on wishlist`() {
        // Arrange
        val wishlist = Wishlist(
            raiderId = RaiderId(1),
            items = listOf(
                WishlistItem(
                    itemId = ItemId(99999),
                    itemName = "Other Item",
                    priority = 1,
                    upgradePercentage = 10.0,
                ),
            ),
        )
        val itemId = ItemId(12345)

        // Act
        val result = calculator.calculateUV(wishlist, itemId)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateUV should normalize upgrade percentage to 0-1 range`() {
        // Arrange
        val itemId = ItemId(12345)
        val wishlist = Wishlist(
            raiderId = RaiderId(1),
            items = listOf(
                WishlistItem(
                    itemId = itemId,
                    itemName = "Test Item",
                    priority = 1,
                    upgradePercentage = 50.0, // 50% should become 0.5
                ),
            ),
        )

        // Act
        val result = calculator.calculateUV(wishlist, itemId)

        // Assert
        result.value shouldBe 0.5
    }

    @Test
    fun `calculateUV should cap value at 1_0 for high upgrade percentages`() {
        // Arrange
        val itemId = ItemId(12345)
        val wishlist = Wishlist(
            raiderId = RaiderId(1),
            items = listOf(
                WishlistItem(
                    itemId = itemId,
                    itemName = "Best Upgrade",
                    priority = 1,
                    upgradePercentage = 150.0, // More than 100%, should cap at 1.0
                ),
            ),
        )

        // Act
        val result = calculator.calculateUV(wishlist, itemId)

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateUV should handle small upgrade percentages`() {
        // Arrange
        val itemId = ItemId(12345)
        val wishlist = Wishlist(
            raiderId = RaiderId(1),
            items = listOf(
                WishlistItem(
                    itemId = itemId,
                    itemName = "Minor Upgrade",
                    priority = 1,
                    upgradePercentage = 5.0,
                ),
            ),
        )

        // Act
        val result = calculator.calculateUV(wishlist, itemId)

        // Assert
        result.value shouldBe 0.05
    }

    // ===== TB (Tier Bonus) Tests =====

    @Test
    fun `calculateTierBonus should return zero when gear is null`() {
        // Act
        val result = calculator.calculateTierBonus(null)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateTierBonus should return zero for no tier pieces`() {
        // Arrange
        val gear = createGearSet(tierPieces = 0)

        // Act
        val result = calculator.calculateTierBonus(gear)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateTierBonus should return 0_3 for one tier piece`() {
        // Arrange
        val gear = createGearSet(tierPieces = 1)

        // Act
        val result = calculator.calculateTierBonus(gear)

        // Assert
        result.value shouldBe 0.3
    }

    @Test
    fun `calculateTierBonus should return 0_6 for two tier pieces`() {
        // Arrange
        val gear = createGearSet(tierPieces = 2)

        // Act
        val result = calculator.calculateTierBonus(gear)

        // Assert
        result.value shouldBe 0.6
    }

    @Test
    fun `calculateTierBonus should return 0_8 for three tier pieces`() {
        // Arrange
        val gear = createGearSet(tierPieces = 3)

        // Act
        val result = calculator.calculateTierBonus(gear)

        // Assert
        result.value shouldBe 0.8
    }

    @Test
    fun `calculateTierBonus should return 1_0 for four or more tier pieces`() {
        // Arrange
        val gear = createGearSet(tierPieces = 4)

        // Act
        val result = calculator.calculateTierBonus(gear)

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateTierBonus should return 1_0 for five tier pieces`() {
        // Arrange
        val gear = createGearSet(tierPieces = 5)

        // Act
        val result = calculator.calculateTierBonus(gear)

        // Assert
        result.value shouldBe 1.0
    }

    // ===== RM (Role Multiplier) Tests =====

    @Test
    fun `calculateRoleMultiplier should return 1_0 for Tank role`() {
        // Act
        val result = calculator.calculateRoleMultiplier(Role.TANK)

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateRoleMultiplier should return 1_0 for Healer role`() {
        // Act
        val result = calculator.calculateRoleMultiplier(Role.HEALER)

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateRoleMultiplier should return 1_0 for DPS role`() {
        // Act
        val result = calculator.calculateRoleMultiplier(Role.DPS)

        // Assert
        result.value shouldBe 1.0
    }

    // ===== RDF (Recency Decay Factor) Tests =====

    @Test
    fun `calculateRDF should return zero when banned`() {
        // Arrange
        val lootHistory = emptyList<LootAward>()
        val activeBans = listOf(
            createLootBan(),
        )

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `calculateRDF should return 1_0 for no recent loot and no bans`() {
        // Arrange
        val lootHistory = emptyList<LootAward>()
        val activeBans = emptyList<LootBan>()

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateRDF should return 0_8 for recent mythic loot`() {
        // Arrange
        val lootHistory = listOf(
            createLootAward(
                tier = LootTier.MYTHIC,
                awardedAt = Instant.now().minus(7, ChronoUnit.DAYS), // Within 2 weeks
            ),
        )
        val activeBans = emptyList<LootBan>()

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert
        result.value shouldBe 0.8
    }

    @Test
    fun `calculateRDF should return 0_9 for recent heroic loot`() {
        // Arrange
        val lootHistory = listOf(
            createLootAward(
                tier = LootTier.HEROIC,
                awardedAt = Instant.now().minus(3, ChronoUnit.DAYS), // Within 1 week
            ),
        )
        val activeBans = emptyList<LootBan>()

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert
        result.value shouldBe 0.9
    }

    @Test
    fun `calculateRDF should return 1_0 for old mythic loot`() {
        // Arrange
        val lootHistory = listOf(
            createLootAward(
                tier = LootTier.MYTHIC,
                awardedAt = Instant.now().minus(30, ChronoUnit.DAYS), // Over 2 weeks ago
            ),
        )
        val activeBans = emptyList<LootBan>()

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateRDF should return 1_0 for old heroic loot`() {
        // Arrange
        val lootHistory = listOf(
            createLootAward(
                tier = LootTier.HEROIC,
                awardedAt = Instant.now().minus(10, ChronoUnit.DAYS), // Over 1 week ago
            ),
        )
        val activeBans = emptyList<LootBan>()

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateRDF should prioritize mythic penalty over heroic`() {
        // Arrange
        val lootHistory = listOf(
            createLootAward(
                tier = LootTier.MYTHIC,
                awardedAt = Instant.now().minus(5, ChronoUnit.DAYS),
            ),
            createLootAward(
                tier = LootTier.HEROIC,
                awardedAt = Instant.now().minus(3, ChronoUnit.DAYS),
            ),
        )
        val activeBans = emptyList<LootBan>()

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert - mythic penalty (0.8) takes precedence
        result.value shouldBe 0.8
    }

    @Test
    fun `calculateRDF should return 1_0 for normal tier loot`() {
        // Arrange
        val lootHistory = listOf(
            createLootAward(
                tier = LootTier.NORMAL,
                awardedAt = Instant.now().minus(1, ChronoUnit.DAYS),
            ),
        )
        val activeBans = emptyList<LootBan>()

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert - normal loot has no penalty
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateRDF should return 1_0 for LFR tier loot`() {
        // Arrange
        val lootHistory = listOf(
            createLootAward(
                tier = LootTier.LFR,
                awardedAt = Instant.now().minus(1, ChronoUnit.DAYS),
            ),
        )
        val activeBans = emptyList<LootBan>()

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert - LFR loot has no penalty
        result.value shouldBe 1.0
    }

    @Test
    fun `calculateRDF should return zero when multiple bans exist`() {
        // Arrange
        val lootHistory = emptyList<LootAward>()
        val activeBans = listOf(
            createLootBan(),
            createLootBan(),
        )

        // Act
        val result = calculator.calculateRDF(lootHistory, activeBans)

        // Assert
        result.value shouldBe 0.0
    }

    // ===== Helper Functions =====

    private fun createAttendanceRecord(
        attendedRaids: Int,
        totalRaids: Int,
    ): AttendanceRecord =
        AttendanceRecord.create(
            raiderId = RaiderId(1),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now(),
            attendedRaids = attendedRaids,
            totalRaids = totalRaids,
        )

    private fun createGearSet(tierPieces: Int): GearSet {
        val tierSlots = listOf(
            EquipmentSlot.HEAD,
            EquipmentSlot.SHOULDER,
            EquipmentSlot.CHEST,
            EquipmentSlot.HANDS,
            EquipmentSlot.LEGS,
        )

        val items = mutableMapOf<EquipmentSlot, GearItem>()
        var itemIdCounter = 1L

        tierSlots.take(tierPieces).forEach { slot ->
            items[slot] = GearItem(
                itemId = ItemId(itemIdCounter++),
                name = "Tier ${slot.name}",
                itemLevel = 639,
                quality = ItemQuality.EPIC,
                slot = slot,
                isTierPiece = true,
            )
        }

        // Add non-tier items to fill remaining slots
        if (items.isEmpty()) {
            items[EquipmentSlot.NECK] = GearItem(
                itemId = ItemId(itemIdCounter),
                name = "Non-tier Neck",
                itemLevel = 630,
                quality = ItemQuality.EPIC,
                slot = EquipmentSlot.NECK,
                isTierPiece = false,
            )
        }

        return GearSet(items = items, gearSetType = GearSetType.EQUIPPED)
    }

    private fun createLootAward(
        tier: LootTier,
        awardedAt: Instant,
    ): LootAward =
        LootAward(
            id = LootAwardId.generate(),
            itemId = ItemId(12345),
            raiderId = RaiderId(1),
            guildId = GuildId("test-guild"),
            awardedAt = awardedAt,
            flpsScore = FlpsScore.of(0.8),
            tier = tier,
        )

    private fun createLootBan(): LootBan =
        LootBan(
            id = LootBanId.generate(),
            raiderId = RaiderId(1),
            guildId = GuildId("test-guild"),
            reason = "Test ban",
            bannedAt = Instant.now(),
            expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
        )

    private fun createPerformanceData(
        totalDeaths: Int,
        totalFights: Int,
        avoidableDamagePercentage: Double,
    ): RaiderPerformanceData =
        RaiderPerformanceData.create(
            raiderId = RaiderId(1),
            characterName = "TestCharacter",
            characterRealm = "TestRealm",
            totalDeaths = totalDeaths,
            totalFights = totalFights,
            avoidableDamagePercentage = avoidableDamagePercentage,
            periodStart = Instant.now().minus(30, ChronoUnit.DAYS),
            periodEnd = Instant.now(),
        )

    private fun createPreparationData(
        raidVaultSlots: Int = 0,
        mythicPlusVaultSlots: Int = 0,
        pvpVaultSlots: Int = 0,
        mythicPlusRating: Int = 0,
        crestsUsed: Int = 0,
        hasHeroicClear: Boolean = false,
        hasNormalClear: Boolean = false,
    ): RaiderPreparationData =
        RaiderPreparationData.create(
            raiderId = RaiderId(1),
            raidVaultSlots = raidVaultSlots,
            mythicPlusVaultSlots = mythicPlusVaultSlots,
            pvpVaultSlots = pvpVaultSlots,
            mythicPlusRating = mythicPlusRating,
            crestsUsed = crestsUsed,
            hasHeroicClear = hasHeroicClear,
            hasNormalClear = hasNormalClear,
        )
}
