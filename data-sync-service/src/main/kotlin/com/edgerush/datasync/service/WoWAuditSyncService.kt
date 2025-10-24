package com.edgerush.datasync.service

import com.edgerush.datasync.api.wowaudit.CharactersResponse
import com.edgerush.datasync.api.wowaudit.LootResponse
import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.datasync.repository.LootAwardRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.OffsetDateTime

@Service
class WoWAuditSyncService(
    private val client: WoWAuditClient,
    private val raiderService: RaiderService,
    private val lootAwardRepository: LootAwardRepository,
    private val syncRunService: SyncRunService
) {

    private val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(javaClass)

    fun syncRoster(): Mono<Void> {
        val run = syncRunService.startRun("wowaudit-roster")
        return client.fetchRoster()
            .flatMap { body ->
                try {
                    val response = objectMapper.readValue(body, CharactersResponse::class.java)
                    response.characters.forEach { raiderService.upsertCharacter(it) }
                    Mono.just(response.characters.size)
                } catch (ex: Exception) {
                    Mono.error(ex)
                }
            }
            .doOnSuccess { count ->
                logger.info("Synced {} raiders from WoWAudit", count)
                syncRunService.complete(run, status = "SUCCESS", message = "Synced $count raiders")
            }
            .doOnError { ex ->
                logger.error("WoWAudit roster sync failed", ex)
                syncRunService.complete(run, status = "FAILED", message = ex.message)
            }
            .then()
    }

    fun syncLootHistory(): Mono<Void> {
        val run = syncRunService.startRun("wowaudit-loot")
        return client.fetchLootHistory()
            .flatMap { body ->
                try {
                    val response = objectMapper.readValue(body, LootResponse::class.java)
                    response.loot.forEach { entry ->
                        val raider = raiderService.upsertCharacter(
                            character = entry.toSyntheticCharacter()
                        )
                        lootAwardRepository.save(
                            LootAwardEntity(
                                raiderId = raider.id!!,
                                itemId = entry.item.id,
                                itemName = entry.item.name,
                                tier = entry.tier,
                                flps = 0.0,
                                rdf = 1.0,
                                awardedAt = entry.awardedAt?.let(OffsetDateTime::parse) ?: OffsetDateTime.now()
                            )
                        )
                    }
                    Mono.just(response.loot.size)
                } catch (ex: Exception) {
                    Mono.error(ex)
                }
            }
            .doOnSuccess { count ->
                logger.info("Synced {} loot entries from WoWAudit", count)
                syncRunService.complete(run, status = "SUCCESS", message = "Synced $count loot entries")
            }
            .doOnError { ex ->
                logger.error("WoWAudit loot sync failed", ex)
                syncRunService.complete(run, status = "FAILED", message = ex.message)
            }
            .then()
    }

    private fun com.edgerush.datasync.api.wowaudit.LootEntryDto.toSyntheticCharacter() =
        com.edgerush.datasync.api.wowaudit.CharacterDto(
            name = this.character,
            realm = "",
            region = "",
            clazz = "",
            spec = "",
            role = "",
            gear = com.edgerush.datasync.api.wowaudit.GearDto(
                equipped = com.edgerush.datasync.api.wowaudit.EquippedGearDto(
                    head = null,
                    neck = null,
                    shoulder = null,
                    back = null,
                    chest = null,
                    wrist = null,
                    hands = null,
                    waist = null,
                    legs = null,
                    feet = null,
                    finger1 = null,
                    finger2 = null,
                    trinket1 = null,
                    trinket2 = null,
                    mainHand = null,
                    offHand = null
                ),
                best = null,
                spark = null
            ),
            statistics = com.edgerush.datasync.api.wowaudit.StatisticsDto(
                warcraftLogs = null,
                mythicPlusScore = null,
                weeklyHighestMPlus = null,
                seasonHighestMPlus = null,
                trackItems = null,
                crestCounts = null,
                vaultSlots = null,
                renown = null,
                pvp = null,
                worldQuests = null,
                raidProgress = null
            ),
            collectibles = com.edgerush.datasync.api.wowaudit.CollectiblesDto(null, null, null, null),
            timestamps = com.edgerush.datasync.api.wowaudit.TimestampDto(null, null)
        )
}
