package com.edgerush.lootman.application.flps

import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.repository.GearRepository
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import com.edgerush.lootman.domain.shared.repository.WishlistRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

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
    private val lootBanRepository: LootBanRepository
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
        lookbackDays: Int = 56
    ): List<RaiderFlpsData> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(lookbackDays.toLong())

        // Get all active raiders in the guild
        val raiders = raiderRepository.findByGuildId(guildId)

        // For each raider, fetch all their data
        return raiders.map { raider ->
            RaiderFlpsData(
                raider = raider,
                attendance = attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                    raider.id, guildId, startDate, endDate
                ),
                lootHistory = lootAwardRepository.findByRaiderId(raider.id),
                wishlist = wishlistRepository.findByRaiderId(raider.id),
                gear = gearRepository.findCurrentGear(raider.id),
                activeBans = lootBanRepository.findActiveByRaiderId(raider.id, guildId)
            )
        }
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
    val activeBans: List<com.edgerush.lootman.domain.loot.model.LootBan>
)
