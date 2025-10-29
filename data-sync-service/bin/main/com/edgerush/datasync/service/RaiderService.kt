package com.edgerush.datasync.service

import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.datasync.entity.RaiderCrestCountEntity
import com.edgerush.datasync.entity.RaiderGearItemEntity
import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import com.edgerush.datasync.entity.RaiderRenownEntity
import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.datasync.entity.RaiderTrackItemEntity
import com.edgerush.datasync.entity.RaiderVaultSlotEntity
import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import com.edgerush.datasync.repository.RaiderCrestCountRepository
import com.edgerush.datasync.repository.RaiderGearItemRepository
import com.edgerush.datasync.repository.RaiderPvpBracketRepository
import com.edgerush.datasync.repository.RaiderRaidProgressRepository
import com.edgerush.datasync.repository.RaiderRenownRepository
import com.edgerush.datasync.repository.RaiderRepository
import com.edgerush.datasync.repository.RaiderStatisticsRepository
import com.edgerush.datasync.repository.RaiderTrackItemRepository
import com.edgerush.datasync.repository.RaiderVaultSlotRepository
import com.edgerush.datasync.repository.RaiderWarcraftLogRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class RaiderService(
    private val raiderRepository: RaiderRepository,
    private val raiderGearItemRepository: RaiderGearItemRepository,
    private val raiderStatisticsRepository: RaiderStatisticsRepository,
    private val raiderTrackItemRepository: RaiderTrackItemRepository,
    private val raiderCrestCountRepository: RaiderCrestCountRepository,
    private val raiderVaultSlotRepository: RaiderVaultSlotRepository,
    private val raiderRenownRepository: RaiderRenownRepository,
    private val raiderRaidProgressRepository: RaiderRaidProgressRepository,
    private val raiderPvpBracketRepository: RaiderPvpBracketRepository,
    private val raiderWarcraftLogRepository: RaiderWarcraftLogRepository
) {

    fun findRaider(name: String, realm: String): RaiderEntity? =
        raiderRepository.findByCharacterNameAndRealm(name, realm).orElse(null)

    fun upsertCharacter(record: RaiderRecord): RaiderEntity {
        val existing = record.wowauditId?.let { id ->
            raiderRepository.findByWowauditId(id).orElse(null)
        } ?: raiderRepository.findByCharacterNameAndRealm(record.name, record.realm).orElse(null)

        val now = OffsetDateTime.now()
        val entity = if (existing != null) {
            existing.copy(
                region = record.region.ifBlank { existing.region },
                wowauditId = record.wowauditId ?: existing.wowauditId,
                clazz = record.clazz.ifBlank { existing.clazz },
                spec = record.spec.ifBlank { existing.spec },
                role = record.role.ifBlank { existing.role },
                rank = record.rank ?: existing.rank,
                status = record.status ?: existing.status,
                note = record.note ?: existing.note,
                blizzardId = record.blizzardId ?: existing.blizzardId,
                trackingSince = record.trackingSince ?: existing.trackingSince,
                joinDate = record.joinDate ?: existing.joinDate,
                blizzardLastModified = record.blizzardLastModified ?: existing.blizzardLastModified,
                lastSync = now
            )
        } else {
            RaiderEntity(
                characterName = record.name,
                realm = record.realm,
                region = record.region,
                wowauditId = record.wowauditId,
                clazz = record.clazz,
                spec = record.spec,
                role = record.role,
                rank = record.rank,
                status = record.status,
                note = record.note,
                blizzardId = record.blizzardId,
                trackingSince = record.trackingSince,
                joinDate = record.joinDate,
                blizzardLastModified = record.blizzardLastModified,
                lastSync = now
            )
        }

        val saved = raiderRepository.save(entity)
        val raiderId = saved.id ?: return saved

        // Gear items
        raiderGearItemRepository.deleteByRaiderId(raiderId)
        if (record.gearItems.isNotEmpty()) {
            raiderGearItemRepository.saveAll(record.gearItems.map { gear ->
                RaiderGearItemEntity(
                    raiderId = raiderId,
                    gearSet = gear.gearSet,
                    slot = gear.slot,
                    itemId = gear.itemId,
                    itemLevel = gear.itemLevel,
                    quality = gear.quality,
                    enchant = gear.enchant,
                    enchantQuality = gear.enchantQuality,
                    upgradeLevel = gear.upgradeLevel,
                    sockets = gear.sockets,
                    name = gear.name
                )
            })
        }

        // Statistics and related collections
        record.statistics?.let { stats ->
            val existingStats = raiderStatisticsRepository.findByRaiderId(raiderId)
            val statsEntity = RaiderStatisticsEntity(
                id = existingStats?.id,
                raiderId = raiderId,
                mythicPlusScore = stats.mythicPlusScore,
                weeklyHighestMplus = stats.weeklyHighestMplus,
                seasonHighestMplus = stats.seasonHighestMplus,
                worldQuestsTotal = stats.worldQuestsTotal,
                worldQuestsThisWeek = stats.worldQuestsThisWeek,
                collectiblesMounts = stats.collectiblesMounts,
                collectiblesToys = stats.collectiblesToys,
                collectiblesUniquePets = stats.collectiblesUniquePets,
                collectiblesLevel25Pets = stats.collectiblesLevel25Pets,
                honorLevel = stats.honorLevel
            )
            raiderStatisticsRepository.save(statsEntity)

            raiderWarcraftLogRepository.deleteByRaiderId(raiderId)
            if (stats.warcraftLogs.isNotEmpty()) {
                raiderWarcraftLogRepository.saveAll(stats.warcraftLogs.map { log ->
                    RaiderWarcraftLogEntity(
                        raiderId = raiderId,
                        difficulty = log.difficulty,
                        score = log.score
                    )
                })
            }

            raiderTrackItemRepository.deleteByRaiderId(raiderId)
            if (stats.trackItems.isNotEmpty()) {
                raiderTrackItemRepository.saveAll(stats.trackItems.map { item ->
                    RaiderTrackItemEntity(
                        raiderId = raiderId,
                        tier = item.tier,
                        itemCount = item.itemCount
                    )
                })
            }

            raiderCrestCountRepository.deleteByRaiderId(raiderId)
            if (stats.crestCounts.isNotEmpty()) {
                raiderCrestCountRepository.saveAll(stats.crestCounts.map { crest ->
                    RaiderCrestCountEntity(
                        raiderId = raiderId,
                        crestType = crest.crestType,
                        crestCount = crest.count
                    )
                })
            }

            raiderVaultSlotRepository.deleteByRaiderId(raiderId)
            if (stats.vaultSlots.isNotEmpty()) {
                raiderVaultSlotRepository.saveAll(stats.vaultSlots.map { slot ->
                    RaiderVaultSlotEntity(
                        raiderId = raiderId,
                        slot = slot.slot,
                        unlocked = slot.unlocked
                    )
                })
            }

            raiderRenownRepository.deleteByRaiderId(raiderId)
            if (stats.renownLevels.isNotEmpty()) {
                raiderRenownRepository.saveAll(stats.renownLevels.map { renown ->
                    RaiderRenownEntity(
                        raiderId = raiderId,
                        faction = renown.faction,
                        level = renown.level
                    )
                })
            }

            raiderRaidProgressRepository.deleteByRaiderId(raiderId)
            if (stats.raidProgress.isNotEmpty()) {
                raiderRaidProgressRepository.saveAll(stats.raidProgress.map { progress ->
                    RaiderRaidProgressEntity(
                        raiderId = raiderId,
                        raid = progress.raid,
                        difficulty = progress.difficulty,
                        bossesDefeated = progress.bossesDefeated
                    )
                })
            }
        } ?: run {
            raiderStatisticsRepository.findByRaiderId(raiderId)?.let { raiderStatisticsRepository.delete(it) }
            raiderWarcraftLogRepository.deleteByRaiderId(raiderId)
            raiderTrackItemRepository.deleteByRaiderId(raiderId)
            raiderCrestCountRepository.deleteByRaiderId(raiderId)
            raiderVaultSlotRepository.deleteByRaiderId(raiderId)
            raiderRenownRepository.deleteByRaiderId(raiderId)
            raiderRaidProgressRepository.deleteByRaiderId(raiderId)
        }

        raiderPvpBracketRepository.deleteByRaiderId(raiderId)
        if (record.pvpBrackets.isNotEmpty()) {
            raiderPvpBracketRepository.saveAll(record.pvpBrackets.map { bracket ->
                RaiderPvpBracketEntity(
                    raiderId = raiderId,
                    bracket = bracket.bracket,
                    rating = bracket.rating,
                    seasonPlayed = bracket.seasonPlayed,
                    weekPlayed = bracket.weekPlayed,
                    maxRating = bracket.maxRating
                )
            })
        }

        return saved
    }

    fun findAllCharacters(): List<RaiderEntity> = 
        raiderRepository.findAll().toList()

    fun findById(id: Long): RaiderEntity? = 
        raiderRepository.findById(id).orElse(null)
}
