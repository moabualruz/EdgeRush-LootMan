package com.edgerush.lootman.application.flps

import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.flps.model.RaiderPreparationData
import com.edgerush.lootman.domain.flps.repository.RaiderPerformanceRepository
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.raider.repository.RaiderCrestCountRepository
import com.edgerush.lootman.domain.raider.repository.RaiderVaultSlotRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.repository.GearRepository
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import com.edgerush.lootman.domain.shared.repository.WishlistRepository
import com.edgerush.lootman.domain.statistics.repository.RaiderStatisticsRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Service to assemble all data needed for FLPS calculations.
 *
 * Coordinates multiple repositories to gather raider data, attendance, loot history,
 * wishlists, gear, and active bans for FLPS scoring.
 */
@Service
class FlpsDataAssemblerService(
    private val raiderRepository: RaiderRepository,
    private val attendanceRepository: AttendanceRepository,
    private val lootAwardRepository: LootAwardRepository,
    private val wishlistRepository: WishlistRepository,
    private val gearRepository: GearRepository,
    private val lootBanRepository: LootBanRepository,
    private val raiderPerformanceRepository: RaiderPerformanceRepository,
    private val raiderStatisticsRepository: RaiderStatisticsRepository,
    private val raiderVaultSlotRepository: RaiderVaultSlotRepository,
    private val raiderCrestCountRepository: RaiderCrestCountRepository,
) {
    /**
     * Assembles complete FLPS calculation data for all raiders in a guild.
     *
     * @param guildId The guild to fetch data for
     * @param lookbackDays Number of days to look back for attendance/loot (default 56 = 8 weeks)
     * @return List of raider data ready for FLPS calculation
     */
    fun assembleFlpsData(
        guildId: GuildId,
        lookbackDays: Int = 56,
    ): List<RaiderFlpsData> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(lookbackDays.toLong())
        val startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        // Get all active raiders in the guild
        val raiders = raiderRepository.findByGuildId(guildId)

        // For each raider, fetch all their data
        return raiders.map { raider ->
            RaiderFlpsData(
                raider = raider,
                attendance =
                    attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                        raider.id,
                        guildId,
                        startDate,
                        endDate,
                    ),
                lootHistory = lootAwardRepository.findByRaiderId(raider.id),
                wishlist = wishlistRepository.findByRaiderId(raider.id),
                gear = gearRepository.findCurrentGear(raider.id),
                activeBans = lootBanRepository.findActiveByRaiderId(raider.id, guildId),
                performanceData = assemblePerformanceData(raider, guildId, startInstant, endInstant),
                preparation = assemblePreparationData(raider),
            )
        }
    }

    /**
     * Assembles performance data for a raider from Warcraft Logs.
     */
    private fun assemblePerformanceData(
        raider: Raider,
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant,
    ): RaiderPerformanceData {
        return raiderPerformanceRepository.findByRaiderAndPeriod(
            raider.id,
            guildId,
            startDate,
            endDate,
        ) ?: RaiderPerformanceData.empty(raider.id, raider.characterName, raider.realm)
    }

    /**
     * Assembles preparation data for a raider from vault slots, statistics, and crest usage.
     */
    private fun assemblePreparationData(raider: Raider): RaiderPreparationData {
        val raiderId = raider.id.value

        // Get vault slots
        val vaultSlots = raiderVaultSlotRepository.findByRaiderId(raiderId, 0, 100)
        val raidVaultSlots = vaultSlots.count { it.slot.startsWith("raid") && it.unlocked == true }
        val mythicPlusVaultSlots = vaultSlots.count { it.slot.startsWith("mythic") && it.unlocked == true }
        val pvpVaultSlots = vaultSlots.count { it.slot.startsWith("pvp") && it.unlocked == true }

        // Get statistics (M+ rating)
        val statistics = raiderStatisticsRepository.findByRaiderId(raiderId)
        val mythicPlusRating = statistics?.mythicPlusScore?.toInt() ?: 0

        // Get crest usage
        val crestCounts = raiderCrestCountRepository.findByRaiderId(raiderId, 0, 100)
        val totalCrestsUsed = crestCounts.sumOf { it.crestCount ?: 0 }

        return RaiderPreparationData.create(
            raiderId = raider.id,
            raidVaultSlots = raidVaultSlots.coerceIn(0, 3),
            mythicPlusVaultSlots = mythicPlusVaultSlots.coerceIn(0, 3),
            pvpVaultSlots = pvpVaultSlots.coerceIn(0, 3),
            mythicPlusRating = mythicPlusRating,
            crestsUsed = totalCrestsUsed,
            hasHeroicClear = false, // TODO: Derive from raid progress data when available
            hasNormalClear = false, // TODO: Derive from raid progress data when available
        )
    }
}

/**
 * Complete data for a raider needed for FLPS calculations.
 */
data class RaiderFlpsData(
    val raider: Raider,
    val attendance: List<com.edgerush.lootman.domain.attendance.model.AttendanceRecord>,
    val lootHistory: List<com.edgerush.lootman.domain.loot.model.LootAward>,
    val wishlist: Wishlist?,
    val gear: GearSet?,
    val activeBans: List<com.edgerush.lootman.domain.loot.model.LootBan>,
    val performanceData: RaiderPerformanceData,
    val preparation: RaiderPreparationData,
)
