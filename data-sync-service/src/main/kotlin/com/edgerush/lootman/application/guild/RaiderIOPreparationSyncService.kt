package com.edgerush.lootman.application.guild

import com.edgerush.datasync.entity.RaiderGearItemEntity
import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.lootman.domain.application.client.RaiderIOClient
import com.edgerush.lootman.domain.application.client.RaiderIONotFoundException
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import com.edgerush.lootman.domain.statistics.repository.RaiderStatisticsRepository
import com.edgerush.lootman.domain.gear.repository.RaiderGearItemRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Syncs raider preparation data (M+ scores, raid progression, gear) from the Raider.IO API.
 *
 * This populates the `raider_statistics` table (feeding EPS calculation) and
 * `raider_gear_items` table (feeding the Gear page).
 * RaiderIO is a free, public API that does not require authentication.
 */
@Service
class RaiderIOPreparationSyncService(
    private val raiderRepository: RaiderRepository,
    private val raiderStatisticsRepository: RaiderStatisticsRepository,
    private val raiderGearItemRepository: RaiderGearItemRepository,
    private val raiderIOClient: RaiderIOClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * RaiderIO slot names mapped to our gear_set slot names.
     * RaiderIO uses: head, neck, shoulder, back, chest, wrist, hands, waist, legs, feet,
     *                finger1, finger2, trinket1, trinket2, mainhand
     * Our DB uses: head, neck, shoulder, back, chest, wrist, hands, waist, legs, feet,
     *              finger_1, finger_2, trinket_1, trinket_2, main_hand
     */
    private val slotMapping = mapOf(
        "head" to "head",
        "neck" to "neck",
        "shoulder" to "shoulder",
        "back" to "back",
        "chest" to "chest",
        "wrist" to "wrist",
        "hands" to "hands",
        "waist" to "waist",
        "legs" to "legs",
        "feet" to "feet",
        "finger1" to "finger_1",
        "finger2" to "finger_2",
        "trinket1" to "trinket_1",
        "trinket2" to "trinket_2",
        "mainhand" to "main_hand",
    )

    /**
     * Syncs M+ scores, raid progression, and gear for all raiders in a guild.
     *
     * Runs on boundedElastic scheduler to avoid blocking reactor threads.
     * Failures for individual raiders are logged but do not abort the sync.
     */
    fun syncPreparationData(guildId: String): Mono<PreparationDataSyncResult> {
        return Mono.fromCallable {
            val raiders = raiderRepository.findByGuildId(GuildId(guildId))
            log.info("Starting RaiderIO preparation sync for guild {}: {} raiders", guildId, raiders.size)

            var synced = 0
            var skipped = 0
            var failed = 0

            for (raider in raiders) {
                try {
                    val profile = raiderIOClient.fetchCharacterProfile(
                        region = raider.region,
                        realm = raider.realm,
                        name = raider.name,
                    ).block() // Safe: running on boundedElastic scheduler

                    if (profile == null) {
                        skipped++
                        continue
                    }

                    val raiderId = raider.id.value
                    val mythicPlusScore = profile.getCurrentMythicPlusScore()

                    // Upsert raider_statistics (M+ scores)
                    val existingStats = raiderStatisticsRepository.findByRaiderId(raiderId)
                    val statsEntity = RaiderStatisticsEntity(
                        id = existingStats?.id,
                        raiderId = raiderId,
                        mythicPlusScore = mythicPlusScore,
                        weeklyHighestMplus = existingStats?.weeklyHighestMplus,
                        seasonHighestMplus = existingStats?.seasonHighestMplus,
                        worldQuestsTotal = existingStats?.worldQuestsTotal,
                        worldQuestsThisWeek = existingStats?.worldQuestsThisWeek,
                        collectiblesMounts = existingStats?.collectiblesMounts,
                        collectiblesToys = existingStats?.collectiblesToys,
                        collectiblesUniquePets = existingStats?.collectiblesUniquePets,
                        collectiblesLevel25Pets = existingStats?.collectiblesLevel25Pets,
                        honorLevel = existingStats?.honorLevel,
                    )
                    raiderStatisticsRepository.save(statsEntity)

                    // Process gear items from RaiderIO profile
                    processGearItems(profile, raiderId)

                    synced++
                } catch (e: RaiderIONotFoundException) {
                    log.debug("Character not found on RaiderIO: {}-{}", raider.name, raider.realm)
                    skipped++
                } catch (e: Exception) {
                    log.warn("Failed to sync RaiderIO data for {} ({}): {}", raider.name, e.javaClass.simpleName, e.message)
                    failed++
                }
            }

            log.info(
                "RaiderIO preparation sync complete for guild {}: synced={}, skipped={}, failed={}",
                guildId, synced, skipped, failed,
            )

            PreparationDataSyncResult(
                synced = synced,
                skipped = skipped,
                failed = failed,
            )
        }.subscribeOn(Schedulers.boundedElastic())
    }

    /**
     * Processes gear items from RaiderIO profile and saves to raider_gear_items table.
     * Deletes existing gear items for the raider first, then inserts new ones.
     */
    private fun processGearItems(
        profile: com.edgerush.lootman.domain.application.client.RaiderIOCharacterProfile,
        raiderId: Long,
    ) {
        val gear = profile.gear ?: return
        val items = gear.items ?: return

        // Delete existing gear items for this raider
        val existingItems = raiderGearItemRepository.findByRaiderId(raiderId, 0, 1000)
        existingItems.forEach { item -> item.id?.let { raiderGearItemRepository.delete(it) } }

        var savedCount = 0
        for ((raiderioSlot, item) in items) {
            if (item == null) continue

            val dbSlot = slotMapping[raiderioSlot] ?: raiderioSlot

            // Skip empty items
            if (item.itemId == null && item.itemLevel == null && item.name == null) continue

            val enchant = item.enchants?.filterNotNull()?.firstOrNull()?.toString()

            raiderGearItemRepository.save(
                RaiderGearItemEntity(
                    raiderId = raiderId,
                    gearSet = "EQUIPPED",
                    slot = dbSlot,
                    itemId = item.itemId,
                    itemLevel = item.itemLevel,
                    quality = item.itemQuality,
                    enchant = enchant,
                    enchantQuality = null,
                    upgradeLevel = null,
                    sockets = item.gems?.filterNotNull()?.size,
                    name = item.name,
                ),
            )
            savedCount++
        }

        if (savedCount > 0) {
            log.debug("Saved {} gear items for {} (raider {})", savedCount, profile.name, raiderId)
        }
    }
}

/**
 * Result of a RaiderIO preparation data sync.
 */
data class PreparationDataSyncResult(
    val synced: Int,
    val skipped: Int,
    val failed: Int,
)
