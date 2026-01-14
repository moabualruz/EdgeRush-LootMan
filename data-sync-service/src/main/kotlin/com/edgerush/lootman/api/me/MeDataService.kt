package com.edgerush.lootman.api.me

import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.attendance.repository.AttendanceStatRepository
import com.edgerush.lootman.domain.gear.repository.RaiderGearItemRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.raider.repository.RaiderVaultSlotRepository
import com.edgerush.lootman.domain.raider.repository.RaiderWarcraftLogRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.repository.WishlistRepository
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Service for aggregating personal data for /me endpoints.
 */
@Service
@Transactional(readOnly = true)
class MeDataService(
    private val raiderRepository: RaiderEntityRepository,
    private val gearItemRepository: RaiderGearItemRepository,
    private val vaultSlotRepository: RaiderVaultSlotRepository,
    private val attendanceStatRepository: AttendanceStatRepository,
    private val attendanceRepository: AttendanceRepository,
    private val warcraftLogRepository: RaiderWarcraftLogRepository,
    private val wishlistRepository: WishlistRepository,
    private val simulationRepository: SimulationRepository,
) {

    /**
     * Get gear data for a raider.
     */
    fun getGearForRaider(guildId: GuildId, raiderId: RaiderId): PersonalGearResponse {
        val raider = raiderRepository.findById(raiderId.value)
            ?: throw IllegalArgumentException("Raider not found: ${raiderId.value}")

        val gearItems = gearItemRepository.findByRaiderId(raiderId.value, 0, 100)

        val missingEnchants = mutableListOf<String>()
        val missingGems = mutableListOf<String>()

        val items = gearItems.mapNotNull { item ->
            val itemId = item.itemId ?: return@mapNotNull null
            val itemLevel = item.itemLevel ?: return@mapNotNull null

            val isEnchanted = item.enchant != null
            val hasSockets = (item.sockets ?: 0) > 0

            // Check for missing enchants on enchantable slots
            val enchantableSlots = setOf("HEAD", "SHOULDER", "BACK", "CHEST", "WRIST", "HANDS", "LEGS", "FEET", "FINGER1", "FINGER2", "MAINHAND")
            if (enchantableSlots.contains(item.slot.uppercase()) && !isEnchanted) {
                missingEnchants.add(item.slot)
            }

            // Check for missing gems
            if (hasSockets) {
                missingGems.add(item.slot)
            }

            // Map quality integer to string
            val qualityName = when (item.quality) {
                1 -> "COMMON"
                2 -> "UNCOMMON"
                3 -> "RARE"
                4 -> "EPIC"
                5 -> "LEGENDARY"
                else -> "EPIC"
            }

            GearItemResponse(
                slot = item.slot,
                itemId = itemId,
                itemName = item.name ?: "Unknown Item",
                itemLevel = itemLevel,
                quality = qualityName,
                enchanted = isEnchanted,
                gemmed = !hasSockets,
                bonusIds = emptyList(),
            )
        }

        val avgIlvl = if (items.isNotEmpty()) items.map { it.itemLevel }.average() else 0.0

        return PersonalGearResponse(
            raiderId = raiderId.value,
            raiderName = raider.characterName,
            characterClass = raider.clazz,
            averageItemLevel = avgIlvl,
            equippedItemLevel = avgIlvl,
            items = items,
            missingEnchants = missingEnchants,
            missingGems = missingGems,
        )
    }

    /**
     * Get vault data for a raider.
     */
    fun getVaultForRaider(guildId: GuildId, raiderId: RaiderId): PersonalVaultResponse {
        val raider = raiderRepository.findById(raiderId.value)
            ?: throw IllegalArgumentException("Raider not found: ${raiderId.value}")

        val vaultSlots = vaultSlotRepository.findByRaiderId(raiderId.value, 0, 20)

        // Group by type based on slot naming convention or default to raid
        val raidSlots = vaultSlots.filter { it.slot.startsWith("RAID") || it.slot.contains("raid", ignoreCase = true) }
            .mapIndexed { index, slot ->
                VaultSlotResponse(
                    slot = index + 1,
                    unlocked = slot.unlocked ?: false,
                    itemLevel = null,
                    progress = 0,
                    required = when (index) { 0 -> 2; 1 -> 4; else -> 6 },
                )
            }

        val mythicPlusSlots = vaultSlots.filter { it.slot.startsWith("M+") || it.slot.contains("mythic", ignoreCase = true) }
            .mapIndexed { index, slot ->
                VaultSlotResponse(
                    slot = index + 1,
                    unlocked = slot.unlocked ?: false,
                    itemLevel = null,
                    progress = 0,
                    required = when (index) { 0 -> 1; 1 -> 4; else -> 8 },
                )
            }

        val pvpSlots = vaultSlots.filter { it.slot.startsWith("PVP") || it.slot.contains("pvp", ignoreCase = true) }
            .mapIndexed { index, slot ->
                VaultSlotResponse(
                    slot = index + 1,
                    unlocked = slot.unlocked ?: false,
                    itemLevel = null,
                    progress = 0,
                    required = when (index) { 0 -> 1250; 1 -> 2500; else -> 5000 },
                )
            }

        return PersonalVaultResponse(
            raiderId = raiderId.value,
            raiderName = raider.characterName,
            raidSlots = raidSlots.ifEmpty { createDefaultVaultSlots(3, listOf(2, 4, 6)) },
            mythicPlusSlots = mythicPlusSlots.ifEmpty { createDefaultVaultSlots(3, listOf(1, 4, 8)) },
            pvpSlots = pvpSlots.ifEmpty { createDefaultVaultSlots(3, listOf(1250, 2500, 5000)) },
        )
    }

    private fun createDefaultVaultSlots(count: Int, requirements: List<Int>): List<VaultSlotResponse> {
        return (1..count).map { slot ->
            VaultSlotResponse(
                slot = slot,
                unlocked = false,
                itemLevel = null,
                progress = 0,
                required = requirements.getOrElse(slot - 1) { 0 },
            )
        }
    }

    /**
     * Get attendance data for a raider.
     */
    fun getAttendanceForRaider(guildId: GuildId, raiderId: RaiderId): PersonalAttendanceResponse {
        val raider = raiderRepository.findById(raiderId.value)
            ?: throw IllegalArgumentException("Raider not found: ${raiderId.value}")

        val stats = attendanceStatRepository.findByCharacterId(raiderId.value, 0, 1).firstOrNull()

        // Get attendance records for the past 90 days
        val startDate = LocalDate.now().minusDays(90)
        val endDate = LocalDate.now()
        val records = attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
            raiderId, guildId, startDate, endDate
        ).sortedByDescending { it.endDate }

        // Calculate totals from aggregated attendance records
        val totalAttended = records.sumOf { it.attendedRaids }
        val totalRaidsFromRecords = records.sumOf { it.totalRaids }
        val overallRate = if (totalRaidsFromRecords > 0) totalAttended.toDouble() / totalRaidsFromRecords else 0.0

        // Use stats if available, otherwise calculate from records
        val attendanceRate = stats?.attendedPercentage?.div(100.0) ?: overallRate
        val totalRaids = stats?.totalAmountOfRaids ?: totalRaidsFromRecords
        val attendedRaids = stats?.attendedAmountOfRaids ?: totalAttended

        // No individual raid status tracking - set breakdown to 0
        return PersonalAttendanceResponse(
            raiderId = raiderId.value,
            raiderName = raider.characterName,
            overallRate = attendanceRate,
            currentStreak = 0, // Not available from aggregated records
            longestStreak = 0, // Not available from aggregated records
            totalRaids = totalRaids,
            attendedRaids = attendedRaids,
            acsScore = attendanceRate,
            breakdown = AttendanceBreakdownResponse(
                present = attendedRaids,
                late = 0, // Not tracked in current model
                excused = 0, // Not tracked in current model
                absent = totalRaids - attendedRaids,
            ),
            recentAttendance = records.take(20).map { record ->
                AttendanceRecordResponse(
                    raidDate = record.endDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
                    raidName = record.instance,
                    status = if (record.attendedRaids > 0) "PRESENT" else "ABSENT",
                    note = null,
                )
            },
        )
    }

    /**
     * Get performance data for a raider.
     * Note: Limited data available from current entity structure.
     */
    fun getPerformanceForRaider(guildId: GuildId, raiderId: RaiderId): PersonalPerformanceResponse {
        val raider = raiderRepository.findById(raiderId.value)
            ?: throw IllegalArgumentException("Raider not found: ${raiderId.value}")

        val logs = warcraftLogRepository.findByRaiderId(raiderId.value, 0, 100)

        // Current entity only has difficulty and score, not detailed fight data
        val avgScore = if (logs.isNotEmpty()) {
            logs.mapNotNull { it.score }.average()
        } else 0.0

        val bestScore = logs.mapNotNull { it.score }.maxOrNull()?.toDouble() ?: 0.0

        // Normalize score to 0-1 range for MAS (scores are typically 0-100)
        val masScore = avgScore / 100.0

        return PersonalPerformanceResponse(
            raiderId = raiderId.value,
            raiderName = raider.characterName,
            characterClass = raider.clazz,
            spec = raider.spec,
            masScore = masScore.coerceIn(0.0, 1.0),
            averagePerformance = avgScore,
            averageItemLevelPerformance = avgScore, // Not available separately
            killCount = logs.size,
            bestPerformance = bestScore,
            recentReports = emptyList(), // Detailed report data not available in entity
            trendData = emptyList(), // Trend data not available in entity
        )
    }

    /**
     * Get wishlist data for a raider.
     */
    fun getWishlistForRaider(guildId: GuildId, raiderId: RaiderId): PersonalWishlistResponse {
        val raider = raiderRepository.findById(raiderId.value)
            ?: throw IllegalArgumentException("Raider not found: ${raiderId.value}")

        val wishlist = wishlistRepository.findByRaiderId(raiderId)
        val items = wishlist?.getItemsByPriority()?.map { item ->
            WishlistItemResponse(
                itemId = item.itemId.value,
                itemName = item.itemName,
                slot = "UNKNOWN",
                priority = item.priority,
                upgradeValue = item.upgradePercentage,
                source = "Unknown",
                boss = null,
                currentItemLevel = null,
                wishlistItemLevel = 0,
                isUpgrade = item.upgradePercentage > 0,
            )
        } ?: emptyList()

        // Check if a simulation profile exists for this character
        val simProfile = simulationRepository.findProfileByCharacter(
            guildId.value,
            raider.characterName,
            raider.realm
        )
        val simulationStatus = if (simProfile != null) {
            SimulationStatusResponse(
                status = "idle",
                lastRun = simProfile.createdAt,
                nextScheduled = null,
                isStale = ChronoUnit.HOURS.between(simProfile.createdAt, Instant.now()) > 24,
            )
        } else {
            null
        }

        return PersonalWishlistResponse(
            raiderId = raiderId.value,
            raiderName = raider.characterName,
            items = items,
            simulationStatus = simulationStatus,
        )
    }
}
