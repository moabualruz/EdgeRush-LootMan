package com.edgerush.datasync.service

import com.edgerush.datasync.api.wowaudit.PeriodResponse
import com.edgerush.datasync.api.wowaudit.TeamResponse
import com.edgerush.datasync.client.WoWAuditClient
import com.edgerush.datasync.entity.*
import com.edgerush.datasync.repository.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.math.roundToInt

@Service
class WoWAuditSyncService(
    private val client: WoWAuditClient,
    private val raiderService: RaiderService,
    private val lootAwardRepository: LootAwardRepository,
    private val lootAwardBonusIdRepository: LootAwardBonusIdRepository,
    private val lootAwardOldItemRepository: LootAwardOldItemRepository,
    private val lootAwardWishDataRepository: LootAwardWishDataRepository,
    private val wishlistSnapshotRepository: WishlistSnapshotRepository,
    private val attendanceStatRepository: AttendanceStatRepository,
    private val raidRepository: RaidRepository,
    private val raidSignupRepository: RaidSignupRepository,
    private val raidEncounterRepository: RaidEncounterRepository,
    private val historicalActivityRepository: HistoricalActivityRepository,
    private val characterHistoryRepository: CharacterHistoryRepository,
    private val guestRepository: GuestRepository,
    private val applicationRepository: ApplicationRepository,
    private val applicationAltRepository: ApplicationAltRepository,
    private val applicationQuestionRepository: ApplicationQuestionRepository,
    private val applicationQuestionFileRepository: ApplicationQuestionFileRepository,
    private val teamMetadataRepository: TeamMetadataRepository,
    private val teamRaidDayRepository: TeamRaidDayRepository,
    private val periodSnapshotRepository: PeriodSnapshotRepository,
    private val snapshotRepository: WoWAuditSnapshotRepository,
    private val syncRunService: SyncRunService
) {

    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val logger = LoggerFactory.getLogger(javaClass)

    fun syncRoster(): Mono<Void> {
        val run = syncRunService.startRun("wowaudit-roster")
        return Mono.zip(fetchTeamInfoOptional(), client.fetchRoster())
            .flatMap { tuple ->
                val team = tuple.t1
                val body = tuple.t2
                Mono.fromCallable {
                    val records = parseRoster(body, team)
                    records.forEach { raiderService.upsertCharacter(it) }
                    saveSnapshot("v1/characters", body)
                    records.size
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
            .onErrorResume { Mono.empty() }
            .then()
    }

    private fun collectGearItems(node: JsonNode?, setName: String): List<RaiderGearItemRecord> {
        if (node == null || node.isMissingNode || node.isNull) return emptyList()
        val slots = listOf(
            "head",
            "neck",
            "shoulder",
            "back",
            "chest",
            "wrist",
            "hands",
            "waist",
            "legs",
            "feet",
            "finger_1",
            "finger_2",
            "trinket_1",
            "trinket_2",
            "main_hand",
            "off_hand"
        )
        return slots.mapNotNull { slot ->
            val slotNode = node.path(slot)
            if (!slotNode.isObject) return@mapNotNull null
            val itemId = slotNode.path("item_id").asLong(-1).takeIf { it > 0 }
            val itemLevel = slotNode.path("item_level").asIntOrNull()
            val quality = slotNode.path("quality").asIntOrNull()
            val enchant = slotNode.path("enchant").asText(null).orNullIfBlank()
            val enchantQuality = slotNode.path("enchant_quality").asIntOrNull()
            val upgradeLevel = slotNode.path("upgrade_level").asIntOrNull()
            val sockets = slotNode.path("sockets").asIntOrNull()
            val name = slotNode.path("name").asText(null).orNullIfBlank()
            if (itemId == null && itemLevel == null && name == null && quality == null) {
                null
            } else {
                RaiderGearItemRecord(
                    gearSet = setName,
                    slot = slot,
                    itemId = itemId,
                    itemLevel = itemLevel,
                    quality = quality,
                    enchant = enchant,
                    enchantQuality = enchantQuality,
                    upgradeLevel = upgradeLevel,
                    sockets = sockets,
                    name = name
                )
            }
        }
    }

    private fun parseRaiderStatistics(element: JsonNode): RaiderStatisticsRecord? {
        val statisticsNode = element.path("statistics")
        val collectiblesNode = element.path("collectibles")

        val mythicPlusScore = statisticsNode.path("mplus_score").asDoubleOrNull()
        val weeklyHighest = statisticsNode.path("weekly_highest_mplus").asIntOrNull()
        val seasonHighest = statisticsNode.path("season_highest_mplus").asIntOrNull()

        val worldQuestsNode = statisticsNode.path("worldQuests").takeIf { it.isObject }
            ?: statisticsNode.path("world_quests").takeIf { it.isObject }
        val worldQuestsTotal = worldQuestsNode?.path("done_total")?.asIntOrNull()
        val worldQuestsThisWeek = worldQuestsNode?.path("this_week")?.asIntOrNull()

        val collectiblesMounts = collectiblesNode.path("mounts").asIntOrNull()
        val collectiblesToys = collectiblesNode.path("toys").asIntOrNull()
        val collectiblesUniquePets = collectiblesNode.path("unique_pets").asIntOrNull()
        val collectiblesLevel25Pets = collectiblesNode.path("lvl_25_pets").asIntOrNull()

        val honorLevel = statisticsNode.path("pvp").path("honor_level").asIntOrNull()

        val warcraftLogs = statisticsNode.path("wcl").takeIf { it.isObject }?.let { wcl ->
            listOf(
                "raid_finder" to wcl.path("raid_finder").asIntOrNull(),
                "normal" to wcl.path("normal").asIntOrNull(),
                "heroic" to wcl.path("heroic").asIntOrNull(),
                "mythic" to wcl.path("mythic").asIntOrNull()
            ).mapNotNull { (difficulty, score) ->
                if (score == null) null else WarcraftLogRecord(difficulty = difficulty, score = score)
            }
        } ?: emptyList()

        val trackItemsNode = statisticsNode.path("trackItems").takeIf { it.isObject }
            ?: statisticsNode.path("track_items").takeIf { it.isObject }
        val trackItemsRecords = trackItemsNode?.let { track ->
            listOf(
                "mythic" to track.path("mythic").asIntOrNull(),
                "heroic" to track.path("heroic").asIntOrNull(),
                "normal" to track.path("normal").asIntOrNull(),
                "raid_finder" to track.path("raid_finder").asIntOrNull()
            ).mapNotNull { (tier, count) ->
                if (count == null) null else TrackItemRecord(tier = tier, itemCount = count)
            }
        } ?: emptyList()

        val crestCountsNode = statisticsNode.path("crestCounts").takeIf { it.isObject }
            ?: statisticsNode.path("crest_counts").takeIf { it.isObject }
        val crestRecords = crestCountsNode?.let { crest ->
            listOf(
                "runed" to crest.path("runed").asIntOrNull(),
                "carved" to crest.path("carved").asIntOrNull(),
                "gilded" to crest.path("gilded").asIntOrNull(),
                "weathered" to crest.path("weathered").asIntOrNull()
            ).mapNotNull { (type, count) ->
                if (count == null) null else CrestCountRecord(crestType = type, count = count)
            }
        } ?: emptyList()

        val vaultSlotsNode = statisticsNode.path("vaultSlots").takeIf { it.isObject }
            ?: statisticsNode.path("vault_slots").takeIf { it.isObject }
        val vaultSlotsRecords = vaultSlotsNode?.let { slots ->
            listOf(
                "slot_1" to slots.path("slot_1").asBooleanOrNull(),
                "slot_2" to slots.path("slot_2").asBooleanOrNull(),
                "slot_3" to slots.path("slot_3").asBooleanOrNull()
            ).mapNotNull { (slot, unlocked) ->
                if (unlocked == null) null else VaultSlotRecord(slot = slot, unlocked = unlocked)
            }
        } ?: emptyList()

        val renownRecords = statisticsNode.path("renown").takeIf { it.isObject }?.let { renown ->
            listOf(
                "assembly_of_the_deeps" to renown.path("assembly_of_the_deeps").asIntOrNull(),
                "council_of_dornogal" to renown.path("council_of_dornogal").asIntOrNull(),
                "hallowfall_arathi" to renown.path("hallowfall_arathi").asIntOrNull(),
                "severed_threads" to renown.path("severed_threads").asIntOrNull(),
                "the_karesh_trust" to renown.path("the_karesh_trust").asIntOrNull()
            ).mapNotNull { (faction, level) ->
                if (level == null) null else RenownRecord(faction = faction, level = level)
            }
        } ?: emptyList()

        val raidProgressNode = statisticsNode.path("raidProgress").takeIf { it.isObject }
            ?: statisticsNode.path("raid_progress").takeIf { it.isObject }
        val raidProgressRecords = parseRaidProgress(raidProgressNode)

        val hasData = listOfNotNull(
            mythicPlusScore,
            weeklyHighest,
            seasonHighest,
            worldQuestsTotal,
            worldQuestsThisWeek,
            collectiblesMounts,
            collectiblesToys,
            collectiblesUniquePets,
            collectiblesLevel25Pets,
            honorLevel
        ).isNotEmpty() || warcraftLogs.isNotEmpty() || trackItemsRecords.isNotEmpty() ||
            crestRecords.isNotEmpty() || vaultSlotsRecords.isNotEmpty() || renownRecords.isNotEmpty() ||
            raidProgressRecords.isNotEmpty()

        if (!hasData) {
            return null
        }

        return RaiderStatisticsRecord(
            mythicPlusScore = mythicPlusScore,
            weeklyHighestMplus = weeklyHighest,
            seasonHighestMplus = seasonHighest,
            worldQuestsTotal = worldQuestsTotal,
            worldQuestsThisWeek = worldQuestsThisWeek,
            collectiblesMounts = collectiblesMounts,
            collectiblesToys = collectiblesToys,
            collectiblesUniquePets = collectiblesUniquePets,
            collectiblesLevel25Pets = collectiblesLevel25Pets,
            honorLevel = honorLevel,
            warcraftLogs = warcraftLogs,
            trackItems = trackItemsRecords,
            crestCounts = crestRecords,
            vaultSlots = vaultSlotsRecords,
            renownLevels = renownRecords,
            raidProgress = raidProgressRecords
        )
    }

    private fun parsePvpBrackets(statisticsNode: JsonNode): List<RaiderPvpBracketRecord> {
        val pvpNode = statisticsNode.path("pvp")
        if (!pvpNode.isObject) return emptyList()
        val brackets = mutableListOf<RaiderPvpBracketRecord>()

        val shuffleNode = pvpNode.path("shuffle")
        if (shuffleNode.isObject) {
            val rating = shuffleNode.path("rating").asIntOrNull()
            val seasonPlayed = shuffleNode.path("season_played").asIntOrNull()
            val weekPlayed = shuffleNode.path("week_played").asIntOrNull()
            if (rating != null || seasonPlayed != null || weekPlayed != null) {
                brackets += RaiderPvpBracketRecord(
                    bracket = "shuffle",
                    rating = rating,
                    seasonPlayed = seasonPlayed,
                    weekPlayed = weekPlayed,
                    maxRating = null
                )
            }
        }

        fun addBracket(name: String, node: JsonNode) {
            if (!node.isObject) return
            val rating = node.path("rating").asIntOrNull()
            val seasonPlayed = node.path("season_played").asIntOrNull()
            val weekPlayed = node.path("week_played").asIntOrNull()
            val maxRating = node.path("max_rating").asIntOrNull()
            if (rating != null || seasonPlayed != null || weekPlayed != null || maxRating != null) {
                brackets += RaiderPvpBracketRecord(
                    bracket = name,
                    rating = rating,
                    seasonPlayed = seasonPlayed,
                    weekPlayed = weekPlayed,
                    maxRating = maxRating
                )
            }
        }

        addBracket("two_v_two", pvpNode.path("two_v_two"))
        addBracket("three_v_three", pvpNode.path("three_v_three"))
        addBracket("rbg", pvpNode.path("rbg"))

        return brackets
    }

    private fun parseRaidProgress(node: JsonNode?): List<RaidProgressRecord> {
        if (node == null || !node.isObject) return emptyList()
        val difficulties = listOf("lfr", "normal", "heroic", "mythic")
        val progress = mutableListOf<RaidProgressRecord>()
        node.fields().forEachRemaining { (raid, value) ->
            if (value.isObject) {
                difficulties.forEach { difficulty ->
                    val defeated = value.path(difficulty).asIntOrNull()
                    if (defeated != null) {
                        progress += RaidProgressRecord(
                            raid = raid,
                            difficulty = difficulty,
                            bossesDefeated = defeated
                        )
                    }
                }
            }
        }
        return progress
    }

    private fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
    fun syncLootHistory(): Mono<Void> {
        val run = syncRunService.startRun("wowaudit-loot")
        return Mono.zip(fetchTeamInfo(), fetchCurrentPeriod())
            .flatMap { tuple ->
                val team = tuple.t1
                val period = tuple.t2
                persistPeriodSnapshot(team, period)
                val seasonId = period.seasonId
                if (seasonId == null) {
                    logger.warn("Season ID missing from period payload; skipping loot history sync")
                    return@flatMap Mono.just(0)
                }
                client.fetchLootHistory(seasonId)
                    .flatMap { body ->
                        Mono.fromCallable {
                            lootAwardRepository.deleteAll()
                            val processed = processLootHistory(body, team, period)
                            saveSnapshot("loot_history/season/$seasonId", body)
                            processed
                        }
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
            .onErrorResume { Mono.empty() }
            .then()
    }

    fun syncWishlists(): Mono<Void> {
        val run = syncRunService.startRun("wowaudit-wishlists")
        return Mono.zip(fetchTeamInfoOptional(), fetchCurrentPeriodOptional(), client.fetchWishlists())
            .flatMapMany { tuple ->
                val team = tuple.t1
                val period = tuple.t2
                val body = tuple.t3
                if (period != null) {
                    persistPeriodSnapshot(team, period)
                }
                saveSnapshot("v1/wishlists", body)
                parseWishlistSummary(body, team)
                    .toFlux()
                    .flatMap({ summary ->
                        client.fetchWishlistDetail(summary.id)
                            .flatMap { payload ->
                                Mono.fromCallable {
                                    saveWishlistSnapshot(summary, payload, team, period)
                                    summary
                                }
                            }
                    }, 4)
            }
            .collectList()
            .doOnSuccess { list ->
                logger.info("Synced {} wishlist details from WoWAudit", list.size)
                syncRunService.complete(run, status = "SUCCESS", message = "Synced ${list.size} wishlist entries")
            }
            .doOnError { ex ->
                logger.error("WoWAudit wishlist sync failed", ex)
                syncRunService.complete(run, status = "FAILED", message = ex.message)
            }
            .onErrorResume { Mono.empty() }
            .then()
    }

    fun syncSupplementalData(): Mono<Void> {
        val run = syncRunService.startRun("wowaudit-supplemental")
        return Mono.zip(fetchTeamInfoOptional(), fetchCurrentPeriodOptional())
            .flatMap { tuple ->
                val team = tuple.t1
                val period = tuple.t2
                if (period != null) {
                    persistPeriodSnapshot(team, period)
                }
                syncAttendanceData(team, period)
                    .then(syncRaidsData(team, period))
                    .then(syncHistoricalData(team, period))
                    .then(syncCharacterHistoryData(team, period))
                    .then(syncGuestsData())
                    .then(syncApplicationsData())
            }
            .doOnSuccess {
                syncRunService.complete(run, status = "SUCCESS", message = "Supplemental WoWAudit data synced")
            }
            .doOnError { ex ->
                logger.error("WoWAudit supplemental sync failed", ex)
                syncRunService.complete(run, status = "FAILED", message = ex.message)
            }
            .onErrorResume { Mono.empty() }
            .then()
    }

    private fun fetchTeamInfoOptional(): Mono<TeamResponse?> =
        fetchTeamInfo().onErrorResume { ex ->
            logger.warn("Unable to fetch WoWAudit team info: ${ex.message}")
            Mono.just(null)
        }

    private fun fetchTeamInfo(): Mono<TeamResponse> =
        client.fetchTeam().flatMap { body ->
            try {
                saveSnapshot("v1/team", body)
                val team = objectMapper.readValue(body, TeamResponse::class.java).withDerivedLocation()
                persistTeamMetadata(team)
                Mono.just(team)
            } catch (ex: Exception) {
                Mono.error(ex)
            }
        }

    private fun fetchCurrentPeriodOptional(): Mono<PeriodResponse?> =
        fetchCurrentPeriod().map { it }.onErrorResume { ex ->
            logger.warn("Unable to fetch WoWAudit period info: ${ex.message}")
            Mono.just(null)
        }

    private fun fetchCurrentPeriod(): Mono<PeriodResponse> =
        client.fetchPeriod().flatMap { body ->
            saveSnapshot("v1/period", body)
            parsePeriod(body)?.let { Mono.just(it) }
                ?: Mono.error(IllegalStateException("Missing season information"))
        }

    private fun persistTeamMetadata(team: TeamResponse) {
        runCatching {
            val now = OffsetDateTime.now()
            teamMetadataRepository.save(
                TeamMetadataEntity(
                    teamId = team.id,
                    guildId = team.guildId,
                    guildName = team.guildName,
                    name = team.name,
                    region = team.region,
                    realm = team.realm,
                    url = team.url,
                    lastRefreshedBlizzard = parseOffsetDateTime(team.lastRefreshed?.blizzard),
                    lastRefreshedPercentiles = parseOffsetDateTime(team.lastRefreshed?.percentiles),
                    lastRefreshedMythicPlus = parseOffsetDateTime(team.lastRefreshed?.mythicPlus),
                    wishlistUpdatedAt = parseEpochSecondsAsOffsetDateTime(team.wishlistUpdatedAt),
                    syncedAt = now
                )
            )
            teamRaidDayRepository.deleteByTeamId(team.id)
            val raidDays = team.raidDays.map { day ->
                TeamRaidDayEntity(
                    teamId = team.id,
                    weekDay = day.weekDay,
                    startTime = parseLocalTime(day.startTime),
                    endTime = parseLocalTime(day.endTime),
                    currentInstance = day.currentInstance,
                    difficulty = day.difficulty,
                    activeFrom = parseLocalDate(day.activeFrom),
                    syncedAt = now
                )
            }
            if (raidDays.isNotEmpty()) {
                teamRaidDayRepository.saveAll(raidDays).forEach { }
            }
        }.onFailure { ex ->
            logger.warn("Failed to persist team metadata for ${team.id}: ${ex.message}")
        }
    }

    private fun TeamResponse.withDerivedLocation(): TeamResponse {
        if (!region.isNullOrBlank() && !realm.isNullOrBlank()) return this
        val (derivedRegion, derivedRealm) = deriveRegionAndRealmFromUrl(url)
        return copy(
            region = region ?: derivedRegion,
            realm = realm ?: derivedRealm
        )
    }

    private fun deriveRegionAndRealmFromUrl(url: String?): Pair<String?, String?> =
        url?.let {
            runCatching {
                val path = URI(it).path.trim('/')
                if (path.isBlank()) return null to null
                val parts = path.split('/')
                val region = parts.getOrNull(0)?.takeIf { segment -> segment.isNotBlank() }
                val realm = parts.getOrNull(1)?.takeIf { segment -> segment.isNotBlank() }
                region to realm
            }.getOrDefault(null to null)
        } ?: (null to null)

    private fun parseEpochSecondsAsOffsetDateTime(value: Long?): OffsetDateTime? =
        value?.let {
            runCatching { OffsetDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC) }.getOrNull()
        }

    private fun persistPeriodSnapshot(team: TeamResponse?, period: PeriodResponse) {
        val teamId = team?.id
        val entity = if (teamId != null && period.periodId != null) {
            val existing = periodSnapshotRepository.findByTeamIdAndPeriodId(teamId, period.periodId)
            PeriodSnapshotEntity(
                id = existing?.id,
                teamId = teamId,
                seasonId = period.seasonId,
                periodId = period.periodId,
                currentPeriod = period.currentPeriod,
                fetchedAt = OffsetDateTime.now()
            )
        } else {
            PeriodSnapshotEntity(
                teamId = teamId,
                seasonId = period.seasonId,
                periodId = period.periodId,
                currentPeriod = period.currentPeriod,
                fetchedAt = OffsetDateTime.now()
            )
        }
        runCatching { periodSnapshotRepository.save(entity) }
            .onFailure { ex ->
                logger.warn("Failed to persist period snapshot for team ${teamId}: ${ex.message}")
            }
    }

    private fun parseRoster(body: String, team: TeamResponse?): List<RaiderRecord> {
        val defaultRegion = team?.region.orEmpty()
        val defaultRealm = team?.realm.orEmpty()
        return try {
            val node = objectMapper.readTree(body)
            if (!node.isArray) {
                return emptyList()
            }
            node.mapNotNull { element ->
                    val name = element.path("name").asText("")
                    if (name.isBlank()) return@mapNotNull null

                    val realm = element.path("realm").asText(defaultRealm)
                    val region = element.path("region").asText(defaultRegion)
                    val clazz = element.path("class").asText("")
                    val spec = element.path("spec").asText("")
                    val role = element.path("role").asText("")
                    val wowauditId = element.path("id").asLong(-1).takeIf { it > 0 }
                    val rank = element.path("rank").asText(null).orNullIfBlank()
                    val status = element.path("status").asText(null).orNullIfBlank()
                    val note = element.path("note").asText(null).orNullIfBlank()
                    val blizzardId = element.path("blizzard_id").asLong(-1).takeIf { it > 0 }
                    val trackingSince = parseOffsetDateTime(element.path("tracking_since").asText(null))

                    val gearItems = mutableListOf<RaiderGearItemRecord>()
                    val gearNode = element.path("gear")
                    gearItems += collectGearItems(gearNode.path("equipped"), "equipped")
                    gearItems += collectGearItems(gearNode.path("best"), "best")
                    gearItems += collectGearItems(gearNode.path("spark"), "spark")

                    val statistics = parseRaiderStatistics(element)
                    val pvpBrackets = parsePvpBrackets(element.path("statistics"))

                    RaiderRecord(
                        wowauditId = wowauditId,
                        name = name,
                        realm = realm,
                        region = region,
                        clazz = clazz,
                        spec = spec,
                        role = role,
                        rank = rank,
                        status = status,
                        note = note,
                        blizzardId = blizzardId,
                        trackingSince = trackingSince,
                        joinDate = parseOffsetDateTime(element.path("timestamps").path("join_date").asText(null)),
                        blizzardLastModified = parseOffsetDateTime(element.path("timestamps").path("blizzard_last_modified").asText(null)),
                        gearItems = gearItems,
                        statistics = statistics,
                        pvpBrackets = pvpBrackets
                    )
                }
        } catch (ex: Exception) {
            logger.error("Failed to parse roster payload", ex)
            emptyList()
        }
    }

    private fun parsePeriod(body: String): PeriodResponse? =
        try {
            val node = objectMapper.readTree(body)
            val seasonId = when {
                node.has("season_id") -> node["season_id"].asLong()
                node.has("current_season") && node["current_season"].has("id") -> node["current_season"]["id"].asLong()
                else -> null
            }
            val currentPeriod = when {
                node.has("current_period") -> node["current_period"].asLong()
                node.has("period_id") -> node["period_id"].asLong()
                else -> null
            }
            val periodId = when {
                node.has("period_id") -> node["period_id"].asLong()
                node.has("period") && node["period"].has("id") -> node["period"]["id"].asLong()
                currentPeriod != null -> currentPeriod
                else -> null
            }
            if (seasonId == null && periodId == null) null else PeriodResponse(seasonId, periodId, currentPeriod)
        } catch (ex: Exception) {
            logger.error("Failed to parse period payload", ex)
            null
        }

    private fun processLootHistory(body: String, team: TeamResponse, period: PeriodResponse): Int {
        return try {
            val root = objectMapper.readTree(body)
            val entriesNode: Sequence<JsonNode> =
                when {
                    root.isArray -> root.arrayElementsOrNull()
                    root.has("history_items") -> root["history_items"].arrayElementsOrNull()
                    root.has("loot") -> root["loot"].arrayElementsOrNull()
                    else -> null
                } ?: return 0
            var processed = 0
            for (node in entriesNode) {
                val characterNode = node.path("character")
                val name = characterNode.path("name").asText("").takeIf { it.isNotBlank() } ?: continue
                val realm = characterNode.path("realm").asText(team.realm.orEmpty()).ifBlank { team.realm.orEmpty() }
                val region = characterNode.path("region").asText(team.region.orEmpty()).ifBlank { team.region.orEmpty() }
                val clazz = characterNode.path("class").asText("").orEmpty()
                val role = characterNode.path("role").asText("").orEmpty()

                val raider = raiderService.upsertCharacter(
                    RaiderRecord(
                        wowauditId = null,
                        name = name,
                        realm = realm,
                        region = region,
                        clazz = clazz,
                        spec = "",
                        role = role,
                        rank = null,
                        status = null,
                        note = null,
                        blizzardId = null,
                        trackingSince = null,
                        joinDate = null,
                        blizzardLastModified = null
                    )
                )
                val raiderId = raider.id ?: continue

                val itemNode = node.path("item")
                if (!itemNode.isObject) continue
                val itemId = itemNode.path("id").asLong(-1).takeIf { it > 0 } ?: continue
                val itemName = itemNode.path("name").asText("").takeIf { it.isNotBlank() } ?: continue

                val tier = node.path("tier").asText("").orEmpty()
                val awardedAt = node.path("awarded_at").asText(null)?.let {
                    runCatching { OffsetDateTime.parse(it) }.getOrElse { parseException ->
                        logger.debug("Unable to parse awarded_at '{}': {}", it, parseException.message)
                        OffsetDateTime.now()
                    }
                } ?: OffsetDateTime.now()

                val responseNode = node.path("response_type")
                val propagatedNode = responseNode.path("propagated_to")

                val difficulty = node.path("difficulty").asText(null).orNullIfBlank()
                val discarded = node.path("discarded").asBooleanOrNull()
                val characterId = node.path("character_id").asLong(-1).takeIf { it > 0 }
                val awardedByCharacterId = node.path("awarded_by_character_id").asLong(-1).takeIf { it > 0 }
                val awardedByName = node.path("awarded_by_name").asText(null).orNullIfBlank()
                val rclootcouncilId = node.path("rclootcouncil_id").asText(null).orNullIfBlank()
                val icon = node.path("icon").asText(null).orNullIfBlank()
                val slot = node.path("slot").asText(null).orNullIfBlank()
                val quality = node.path("quality").asText(null).orNullIfBlank()
                val note = node.path("note").asText(null).orNullIfBlank()
                val sameResponseAmount = node.path("same_response_amount").asIntOrNull()
                val wishValue = node.path("wish_value").asDoubleOrNull()?.roundToInt()
                val flps = node.path("flps").asDoubleOrNull() ?: 0.0
                val rdf = node.path("rdf").asDoubleOrNull() ?: 1.0

                val savedAward = lootAwardRepository.save(
                    LootAwardEntity(
                        raiderId = raiderId,
                        itemId = itemId,
                        itemName = itemName,
                        tier = tier,
                        flps = flps,
                        rdf = rdf,
                        awardedAt = awardedAt,
                        rclootcouncilId = rclootcouncilId,
                        icon = icon,
                        slot = slot,
                        quality = quality,
                        responseTypeId = responseNode.path("id").asIntOrNull(),
                        responseTypeName = responseNode.path("name").asText(null).orNullIfBlank(),
                        responseTypeRgba = responseNode.path("rgba").asText(null).orNullIfBlank(),
                        responseTypeExcluded = responseNode.path("excluded").asBooleanOrNull(),
                        propagatedResponseTypeId = propagatedNode.path("id").asIntOrNull(),
                        propagatedResponseTypeName = propagatedNode.path("name").asText(null).orNullIfBlank(),
                        propagatedResponseTypeRgba = propagatedNode.path("rgba").asText(null).orNullIfBlank(),
                        propagatedResponseTypeExcluded = propagatedNode.path("excluded").asBooleanOrNull(),
                        sameResponseAmount = sameResponseAmount,
                        note = note,
                        wishValue = wishValue,
                        difficulty = difficulty,
                        discarded = discarded,
                        characterId = characterId,
                        awardedByCharacterId = awardedByCharacterId,
                        awardedByName = awardedByName
                    )
                )

                val awardId = savedAward.id
                if (awardId != null) {
                    val aggregatedBonusIds = linkedSetOf<String?>()
                    listOf(node.path("bonus_ids"), itemNode.path("bonus_ids"))
                        .filter { it.isArray }
                        .forEach { bonusNode: JsonNode ->
                            bonusNode.forEach { element: JsonNode ->
                                aggregatedBonusIds += element.asText(null).orNullIfBlank()
                            }
                        }

                    if (aggregatedBonusIds.isNotEmpty()) {
                        val bonusEntities = aggregatedBonusIds.map { bonusId ->
                            LootAwardBonusIdEntity(lootAwardId = awardId, bonusId = bonusId)
                        }
                        lootAwardBonusIdRepository.saveAll(bonusEntities)
                    }

                    val oldItemsNode = node.path("old_items")
                    if (oldItemsNode.isArray) {
                        val oldItemEntities = mutableListOf<LootAwardOldItemEntity>()
                        oldItemsNode.forEach { oldItem: JsonNode ->
                            val oldItemId = oldItem.path("item_id").asLong(-1).takeIf { it > 0 }
                            val bonusArray = oldItem.path("bonus_ids")
                            if (bonusArray.isArray && bonusArray.size() > 0) {
                                bonusArray.forEach { bonusNode: JsonNode ->
                                    oldItemEntities += LootAwardOldItemEntity(
                                        lootAwardId = awardId,
                                        itemId = oldItemId,
                                        bonusId = bonusNode.asText(null).orNullIfBlank()
                                    )
                                }
                            } else {
                                oldItemEntities += LootAwardOldItemEntity(
                                    lootAwardId = awardId,
                                    itemId = oldItemId,
                                    bonusId = null
                                )
                            }
                        }
                        if (oldItemEntities.isNotEmpty()) {
                            lootAwardOldItemRepository.saveAll(oldItemEntities)
                        }
                    }

                    val wishDataNode = node.path("wish_data")
                    val wishEntries = when {
                        wishDataNode.isArray -> wishDataNode.elements().asSequence().toList()
                        wishDataNode.isObject -> wishDataNode.fields().asSequence().map { it.value }.toList()
                        else -> emptyList()
                    }
                    if (wishEntries.isNotEmpty()) {
                        val wishEntities = wishEntries.mapNotNull { wish ->
                            val specName = wish.path("spec_name").asText(null).orNullIfBlank()
                            val specIcon = wish.path("spec_icon").asText(null).orNullIfBlank()
                            val value = wish.path("value").asDoubleOrNull()?.roundToInt()
                            if (specName == null && specIcon == null && value == null) {
                                null
                            } else {
                                LootAwardWishDataEntity(
                                    lootAwardId = awardId,
                                    specName = specName,
                                    specIcon = specIcon,
                                    value = value
                                )
                            }
                        }
                        if (wishEntities.isNotEmpty()) {
                            lootAwardWishDataRepository.saveAll(wishEntities)
                        }
                    }
                }

                processed++
            }
            processed
        } catch (ex: Exception) {
            logger.error("Failed to process loot history", ex)
            0
        }
    }

    private fun parseWishlistSummary(body: String, team: TeamResponse?): List<WishlistSummary> {
        val defaultRealm = team?.realm.orEmpty()
        val defaultRegion = team?.region.orEmpty()
        val result = mutableListOf<WishlistSummary>()
        return try {
            val node = objectMapper.readTree(body)
            val characters = when {
                node.isArray -> node
                node.has("characters") -> node["characters"]
                else -> null
            } ?: return emptyList()
            characters.forEach { element: JsonNode ->
                val id = element.path("id").asLong(-1)
                if (id <= 0) return@forEach
                val name = element.path("name").asText("")
                if (name.isBlank()) return@forEach
                val realm = element.path("realm").asText(defaultRealm)
                val region = element.path("region").asText(defaultRegion)
                result += WishlistSummary(id, name, realm, region)
            }
            result
        } catch (ex: Exception) {
            logger.error("Failed to parse wishlist summary payload", ex)
            emptyList()
        }
    }

    private fun saveWishlistSnapshot(summary: WishlistSummary, payload: String, team: TeamResponse?, period: PeriodResponse?) {
        saveSnapshot("v1/wishlists/${summary.id}", payload)
        val detailNode = try {
            objectMapper.readTree(payload)
        } catch (ex: Exception) {
            logger.error("Failed to parse wishlist detail for {}", summary.name, ex)
            return
        }
        val characterNode = detailNode.path("character")
        val name = characterNode.path("name").asText(summary.name)
        val realm = characterNode.path("realm").asText(summary.realm)
        val region = characterNode.path("region").asText(summary.region)
        val clazz = characterNode.path("class").asText("")
        val role = characterNode.path("role").asText("")
        val teamId = team?.id
        val seasonId = period?.seasonId
        val periodId = period?.periodId ?: period?.currentPeriod

        val raider = raiderService.upsertCharacter(
            RaiderRecord(
                wowauditId = null,
                name = name,
                realm = realm,
                region = region,
                clazz = clazz,
                spec = "",
                role = role,
                rank = null,
                status = null,
                note = null,
                blizzardId = null,
                trackingSince = null,
                joinDate = null,
                blizzardLastModified = null,
                gearItems = emptyList(),
                statistics = null,
                pvpBrackets = emptyList()
            )
        )

        wishlistSnapshotRepository.deleteByCharacterNameAndCharacterRealm(name, realm)
        wishlistSnapshotRepository.save(
            WishlistSnapshotEntity(
                raiderId = raider.id,
                characterName = name,
                characterRealm = realm,
                characterRegion = region,
                teamId = teamId,
                seasonId = seasonId,
                periodId = periodId,
                rawPayload = payload,
                syncedAt = OffsetDateTime.now()
            )
        )
    }

    private fun syncAttendanceData(team: TeamResponse?, period: PeriodResponse?): Mono<Unit> =
        client.fetchAttendance()
            .flatMap { body ->
                Mono.fromCallable {
                    saveSnapshot("v1/attendance", body)
                    val records = parseAttendance(body, team, period)
                    attendanceStatRepository.deleteAll()
                    attendanceStatRepository.saveAll(records.map { it.toEntity() }).forEach { }
                    logger.info("Synced {} attendance records", records.size)
                    Unit
                }
            }

    private fun syncRaidsData(team: TeamResponse?, period: PeriodResponse?): Mono<Unit> {
        if (team == null) {
            logger.warn("Team information unavailable; skipping raid sync")
            return Mono.empty()
        }
        return client.fetchRaids()
            .flatMapMany { body ->
                saveSnapshot("v1/raids?include_past=true", body)
                val summaries = parseRaidList(body)
                raidEncounterRepository.deleteAll()
                raidSignupRepository.deleteAll()
                raidRepository.deleteAll()
                summaries.toFlux()
            }
            .flatMap({ summary ->
                client.fetchRaidDetail(summary.id)
                    .map { detail -> summary to detail }
            }, 2)
            .flatMap { (summary, detail) ->
                Mono.fromCallable { saveRaid(summary, detail, team, period) }
            }
            .count()
            .doOnNext { count -> logger.info("Synced {} raids from WoWAudit", count) }
            .thenReturn(Unit)
    }

    private fun syncHistoricalData(team: TeamResponse?, period: PeriodResponse?): Mono<Unit> {
        val periodId = period?.periodId ?: period?.currentPeriod
        if (periodId == null) {
            logger.warn("Period information unavailable; skipping historical activity sync")
            return Mono.empty()
        }
        return client.fetchHistoricalData(periodId)
            .flatMap { body ->
                Mono.fromCallable {
                    saveSnapshot("v1/historical_data?period=$periodId", body)
                    val records = parseHistoricalData(body, periodId, team?.id, period?.seasonId)
                    historicalActivityRepository.deleteAll()
                    historicalActivityRepository.saveAll(records).forEach { }
                    logger.info("Synced {} historical activity records", records.size)
                    Unit
                }
            }
    }

    private fun syncCharacterHistoryData(team: TeamResponse?, period: PeriodResponse?): Mono<Unit> {
        return Mono.fromCallable {
            val characters = raiderService.findAllCharacters().filter { raider -> raider.wowauditId != null }
            var syncedCount = 0
            
            characters.forEach { raider ->
                try {
                    val body = client.fetchCharacterHistory(raider.wowauditId!!).block()
                    if (body != null) {
                        saveSnapshot("v1/historical_data/${raider.wowauditId}", body)
                        val (historyJson, bestGearJson) = parseCharacterHistory(body)
                        
                        characterHistoryRepository.deleteByCharacterId(raider.wowauditId!!)
                        characterHistoryRepository.save(
                            CharacterHistoryEntity(
                                characterId = raider.wowauditId!!,
                                characterName = raider.characterName,
                                characterRealm = raider.realm,
                                characterRegion = raider.region,
                                teamId = team?.id,
                                seasonId = period?.seasonId,
                                periodId = period?.periodId ?: period?.currentPeriod,
                                historyJson = historyJson,
                                bestGearJson = bestGearJson
                            )
                        )
                        syncedCount++
                    }
                } catch (ex: Exception) {
                    logger.warn("Failed to sync character history for ${raider.characterName}: ${ex.message}")
                }
            }
            
            logger.info("Synced {} character histories", syncedCount)
            Unit
        }
    }

    private fun syncGuestsData(): Mono<Unit> =
        client.fetchGuests()
            .flatMap { body ->
                Mono.fromCallable {
                    saveSnapshot("v1/guests", body)
                    val guests = parseGuests(body)
                    guestRepository.deleteAll()
                    guestRepository.saveAll(guests).forEach { }
                    logger.info("Synced {} guests", guests.size)
                    Unit
                }
            }

    private fun syncApplicationsData(): Mono<Unit> =
        client.fetchApplications()
            .flatMap { body ->
                Mono.fromCallable {
                    saveSnapshot("v1/applications", body)
                    parseApplicationSummaries(body)
                }
            }
            .flatMapMany { summaries ->
                applicationQuestionRepository.deleteAll()
                applicationAltRepository.deleteAll()
                applicationRepository.deleteAll()
                applicationQuestionFileRepository.deleteAll()
                summaries.toFlux()
            }
            .flatMap({ summary ->
                client.fetchApplicationDetail(summary.id)
                    .map { detail -> summary to detail }
            }, 2)
            .flatMap { (summary, detail) ->
                Mono.fromCallable { saveApplication(summary, detail) }
            }
            .count()
            .doOnNext { count -> logger.info("Synced {} applications", count) }
            .thenReturn(Unit)

    private fun parseAttendance(body: String, team: TeamResponse?, period: PeriodResponse?): List<AttendanceRecord> {
        val defaultRealm = team?.realm.orEmpty()
        val defaultRegion = team?.region.orEmpty()
        val seasonId = period?.seasonId
        val periodId = period?.periodId ?: period?.currentPeriod
        val records = mutableListOf<AttendanceRecord>()
        return try {
            val node = objectMapper.readTree(body)
            val instance = node.path("instance").asText(null)
            val encounter = node.path("encounter").asText(null)
            val startDate = node.path("start_date").asText(null)
            val endDate = node.path("end_date").asText(null)
            val charactersNode = node.path("characters")
            val characters = charactersNode.arrayElementsOrNull() ?: return emptyList()
            characters.forEach { element ->
                val name = element.path("name").asText("")
                if (name.isBlank()) return@forEach
                val realm = element.path("realm").asText(defaultRealm)
                val region = element.path("region").asText(defaultRegion).ifBlank { defaultRegion }
                val clazz = element.path("class").asText("")
                val role = element.path("role").asText("")
                records += AttendanceRecord(
                    instance = instance,
                    encounter = encounter,
                    startDate = startDate,
                    endDate = endDate,
                    characterId = element.path("id").asLong(-1).takeIf { it > 0 },
                    characterName = name,
                    characterRealm = realm,
                    characterRegion = region,
                    characterClass = clazz,
                    characterRole = role,
                    attendedAmountOfRaids = element.path("attended_amount_of_raids").asIntOrNull(),
                    totalAmountOfRaids = element.path("total_amount_of_raids").asIntOrNull(),
                    attendedPercentage = element.path("attended_percentage").asDoubleOrNull(),
                    selectedAmountOfEncounters = element.path("selected_amount_of_encounters").asIntOrNull(),
                    totalAmountOfEncounters = element.path("total_amount_of_encounters").asIntOrNull(),
                    selectedPercentage = element.path("selected_percentage").asDoubleOrNull(),
                    teamId = team?.id,
                    seasonId = seasonId,
                    periodId = periodId
                )
            }
            records
        } catch (ex: Exception) {
            logger.error("Failed to parse attendance payload", ex)
            emptyList()
        }
    }

    private fun parseRaidList(body: String): List<RaidSummary> {
        val result = mutableListOf<RaidSummary>()
        return try {
            val node = objectMapper.readTree(body)
            val raidsNode = when {
                node.isArray -> node
                node.has("raids") -> node["raids"]
                else -> null
            } ?: return emptyList()
            raidsNode.forEach { element: JsonNode ->
                val id = element.path("id").asLong(-1)
                if (id <= 0) return@forEach
                result += RaidSummary(
                    id = id,
                    date = element.path("date").asText(null),
                    startTime = element.path("start_time").asText(null),
                    endTime = element.path("end_time").asText(null),
                    instance = element.path("instance").asText(null),
                    difficulty = element.path("difficulty").asText(null),
                    optional = element.path("optional").asBooleanOrNull(),
                    status = element.path("status").asText(null),
                    presentSize = element.path("present_size").asIntOrNull(),
                    totalSize = element.path("total_size").asIntOrNull(),
                    notes = element.path("notes").asText(null),
                    selectionsImage = element.path("selections_image").asText(null)
                )
            }
            result
        } catch (ex: Exception) {
            logger.error("Failed to parse raids payload", ex)
            emptyList()
        }
    }

    private fun saveRaid(summary: RaidSummary, detailBody: String, team: TeamResponse, period: PeriodResponse?) {
        val detailNode = try {
            objectMapper.readTree(detailBody)
        } catch (ex: Exception) {
            logger.error("Failed to parse raid detail for {}", summary.id, ex)
            return
        }
        saveSnapshot("v1/raids/${summary.id}", detailBody)
        try {
            raidRepository.deleteById(summary.id)
        } catch (_: Exception) {
        }
        val date = parseLocalDate(detailNode.path("date").asText(summary.date))
        val startTime = parseLocalTime(detailNode.path("start_time").asText(summary.startTime))
        val endTime = parseLocalTime(detailNode.path("end_time").asText(summary.endTime))
        val instance = detailNode.path("instance").asText(summary.instance)
        val difficulty = detailNode.path("difficulty").asText(summary.difficulty)
        val optional = detailNode.path("optional").asBooleanOrNull() ?: summary.optional
        val status = detailNode.path("status").asText(summary.status)
        val presentSize = detailNode.path("present_size").asIntOrNull() ?: summary.presentSize
        val totalSize = detailNode.path("total_size").asIntOrNull() ?: summary.totalSize
        val notes = detailNode.path("notes").asText(summary.notes)
        val selectionsImage = detailNode.path("selections_image").asText(summary.selectionsImage)
        val createdAt = parseOffsetDateTime(detailNode.path("created_at").asText(null))
        val updatedAt = parseOffsetDateTime(detailNode.path("updated_at").asText(null))
        val seasonId = period?.seasonId
        val periodId = period?.periodId ?: period?.currentPeriod

        raidRepository.save(
            RaidEntity(
                raidId = summary.id,
                date = date,
                startTime = startTime,
                endTime = endTime,
                instance = instance,
                difficulty = difficulty,
                optional = optional,
                status = status,
                presentSize = presentSize,
                totalSize = totalSize,
                notes = notes,
                selectionsImage = selectionsImage,
                teamId = team.id,
                seasonId = seasonId,
                periodId = periodId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                syncedAt = OffsetDateTime.now()
            )
        )

        val signupsNode = detailNode.path("signups")
        if (signupsNode.isArray) {
            val entities = signupsNode.map { signup ->
                val character = signup.path("character")
                RaidSignupEntity(
                    raidId = summary.id,
                    characterId = character.path("id").asLong(-1).takeIf { it > 0 },
                    characterName = character.path("name").asText(null),
                    characterRealm = character.path("realm").asText(null),
                    characterRegion = character.path("region").asText(null),
                    characterClass = character.path("class").asText(null),
                    characterRole = character.path("role").asText(null),
                    characterGuest = character.path("guest").asBooleanOrNull(),
                    status = signup.path("status").asText(null),
                    comment = signup.path("comment").asText(null),
                    selected = signup.path("selected").asBooleanOrNull()
                )
            }
            raidSignupRepository.saveAll(entities).forEach { }
        }

        val encountersNode = detailNode.path("encounters")
        if (encountersNode.isArray) {
            val entities = encountersNode.map { encounter ->
                RaidEncounterEntity(
                    raidId = summary.id,
                    encounterId = encounter.path("id").asLong(-1).takeIf { it > 0 },
                    name = encounter.path("name").asText(null),
                    enabled = encounter.path("enabled").asBooleanOrNull(),
                    extra = encounter.path("extra").asBooleanOrNull(),
                    notes = encounter.path("notes").asText(null)
                )
            }
            raidEncounterRepository.saveAll(entities).forEach { }
        }
    }

    private fun parseHistoricalData(body: String, periodId: Long, teamId: Long?, seasonId: Long?): List<HistoricalActivityEntity> {
        val result = mutableListOf<HistoricalActivityEntity>()
        return try {
            val node = objectMapper.readTree(body)
            val characters = when {
                node.isArray -> node
                node.has("characters") -> node["characters"]
                else -> null
            } ?: return emptyList()
            characters.forEach { character: JsonNode ->
                val name = character.path("name").asText("")
                if (name.isBlank()) return@forEach
                val realm = character.path("realm").asText(null)
                val characterId = character.path("id").asLong(-1).takeIf { it > 0 }
                val dataNode = character.path("data")
                result += HistoricalActivityEntity(
                    characterId = characterId,
                    characterName = name,
                    characterRealm = realm,
                    periodId = periodId,
                    teamId = teamId,
                    seasonId = seasonId,
                    dataJson = dataNode.toString(),
                    syncedAt = OffsetDateTime.now()
                )
            }
            result
        } catch (ex: Exception) {
            logger.error("Failed to parse historical data payload", ex)
            emptyList()
        }
    }

    private fun parseGuests(body: String): List<GuestEntity> {
        val guests = mutableListOf<GuestEntity>()
        return try {
            val node = objectMapper.readTree(body)
            if (!node.isArray) {
                return emptyList()
            }
            node.forEach { guestNode: JsonNode ->
                val id = guestNode.path("id").asLong(-1)
                if (id <= 0) return@forEach
                guests += GuestEntity(
                    guestId = id,
                    name = guestNode.path("name").asText("Unknown"),
                    realm = guestNode.path("realm").asText(null),
                    `class` = guestNode.path("class").asText(null),
                    role = guestNode.path("role").asText(null),
                    blizzardId = guestNode.path("blizzard_id").asLong(-1).takeIf { it > 0 },
                    trackingSince = parseOffsetDateTime(guestNode.path("tracking_since").asText(null)),
                    syncedAt = OffsetDateTime.now()
                )
            }
            guests
        } catch (ex: Exception) {
            logger.error("Failed to parse guest payload", ex)
            emptyList()
        }
    }

    private fun parseApplicationSummaries(body: String): List<ApplicationSummary> {
        val summaries = mutableListOf<ApplicationSummary>()
        return try {
            val node = objectMapper.readTree(body)
            val applications = when {
                node.isArray -> node
                node.has("applications") -> node["applications"]
                else -> null
            } ?: return emptyList()
            applications.forEach { app: JsonNode ->
                val id = app.path("id").asLong(-1)
                if (id <= 0) return@forEach
                val main = app.path("main_character")
                summaries += ApplicationSummary(
                    id = id,
                    appliedAt = parseOffsetDateTime(app.path("applied_at").asText(null)),
                    status = app.path("status").asText(null),
                    role = app.path("role").asText(null),
                    age = app.path("age").asIntOrNull(),
                    country = app.path("country").asText(null),
                    battletag = app.path("battletag").asText(null),
                    discordId = app.path("discord_id").asText(null),
                    mainCharacterName = main.path("name").asText(null).orNullIfBlank(),
                    mainCharacterRealm = main.path("realm").asText(null).orNullIfBlank(),
                    mainCharacterClass = main.path("class").asText(null).orNullIfBlank(),
                    mainCharacterRole = main.path("role").asText(null).orNullIfBlank(),
                    mainCharacterRace = main.path("race").asText(null).orNullIfBlank(),
                    mainCharacterFaction = main.path("faction").asText(null).orNullIfBlank(),
                    mainCharacterLevel = main.path("level").asIntOrNull(),
                    mainCharacterRegion = main.path("region").asText(null).orNullIfBlank()
                )
            }
            summaries
        } catch (ex: Exception) {
            logger.error("Failed to parse applications payload", ex)
            emptyList()
        }
    }

    private fun saveApplication(summary: ApplicationSummary, detailBody: String) {
        saveSnapshot("v1/applications/${summary.id}", detailBody)
        val detailNode = try {
            objectMapper.readTree(detailBody)
        } catch (ex: Exception) {
            logger.error("Failed to parse application detail for {}", summary.id, ex)
            return
        }
        val mainDetail = detailNode.path("main_character")
        val enrichedSummary = if (mainDetail.isObject) {
            summary.copy(
                mainCharacterRace = mainDetail.path("race").asText(summary.mainCharacterRace).orNullIfBlank(),
                mainCharacterFaction = mainDetail.path("faction").asText(summary.mainCharacterFaction).orNullIfBlank(),
                mainCharacterLevel = mainDetail.path("level").asIntOrNull() ?: summary.mainCharacterLevel,
                mainCharacterRegion = mainDetail.path("region").asText(summary.mainCharacterRegion).orNullIfBlank()
            )
        } else {
            summary
        }

        applicationRepository.save(enrichedSummary.toEntity())

        val altsNode = detailNode.path("alts")
        if (altsNode.isArray) {
            val alts = altsNode.map { alt ->
                ApplicationAltEntity(
                    applicationId = summary.id,
                    name = alt.path("name").asText(null).orNullIfBlank(),
                    realm = alt.path("realm").asText(null).orNullIfBlank(),
                    region = alt.path("region").asText(null).orNullIfBlank(),
                    `class` = alt.path("class").asText(null).orNullIfBlank(),
                    role = alt.path("role").asText(null).orNullIfBlank(),
                    level = alt.path("level").asIntOrNull(),
                    faction = alt.path("faction").asText(null).orNullIfBlank(),
                    race = alt.path("race").asText(null).orNullIfBlank()
                )
            }
            applicationAltRepository.saveAll(alts).forEach { }
        }

        val questionsNode = detailNode.path("questions")
        applicationQuestionFileRepository.deleteByApplicationId(summary.id)
        if (questionsNode.isArray) {
            val questionFiles = mutableListOf<ApplicationQuestionFileEntity>()
            val questions = questionsNode.mapIndexed { index, question ->
                val filesJson = if (question.has("files")) objectMapper.writeValueAsString(question["files"]) else null
                val filesNode = question.path("files")
                if (filesNode.isArray) {
                    filesNode.forEach { fileNode ->
                        questionFiles += ApplicationQuestionFileEntity(
                            applicationId = summary.id,
                            questionPosition = index,
                            question = question.path("question").asText(null).orNullIfBlank(),
                            originalFilename = fileNode.path("original_filename").asText(null).orNullIfBlank(),
                            url = fileNode.path("url").asText(null).orNullIfBlank()
                        )
                    }
                }
                ApplicationQuestionEntity(
                    applicationId = summary.id,
                    position = index,
                    question = question.path("question").asText(null).orNullIfBlank(),
                    answer = question.path("answer").asText(null).orNullIfBlank(),
                    filesJson = filesJson
                )
            }
            applicationQuestionRepository.saveAll(questions).forEach { }
            if (questionFiles.isNotEmpty()) {
                applicationQuestionFileRepository.saveAll(questionFiles).forEach { }
            }
        }
    }

    private fun saveSnapshot(endpoint: String, payload: String) {
        snapshotRepository.save(
            WoWAuditSnapshotEntity(
                endpoint = endpoint,
                rawPayload = payload,
                syncedAt = OffsetDateTime.now()
            )
        )
    }

    private fun saveSnapshotMono(endpoint: String, payload: String): Mono<Unit> =
        Mono.fromCallable { saveSnapshot(endpoint, payload) }
            .thenReturn(Unit)

    private fun extractApplicationIds(body: String): List<Long> =
        try {
            val node = objectMapper.readTree(body)
            val applications = when {
                node.isArray -> node
                node.has("applications") -> node["applications"]
                else -> return emptyList()
            }
            applications.mapNotNull { it.path("id").asLong(-1).takeIf { id -> id > 0 } }
        } catch (ex: Exception) {
            logger.error("Failed to parse applications payload", ex)
            emptyList()
        }

    private fun parseCharacterHistory(body: String): Pair<String, String?> {
        return try {
            val node = objectMapper.readTree(body)
            val historyNode = node.path("history")
            val bestGearNode = node.path("best_gear")
            
            val historyJson = if (historyNode.isMissingNode || historyNode.isNull) {
                "[]"
            } else {
                objectMapper.writeValueAsString(historyNode)
            }
            
            val bestGearJson = if (bestGearNode.isMissingNode || bestGearNode.isNull) {
                null
            } else {
                objectMapper.writeValueAsString(bestGearNode)
            }
            
            historyJson to bestGearJson
        } catch (ex: Exception) {
            logger.error("Failed to parse character history payload", ex)
            "[]" to null
        }
    }

    private fun JsonNode.arrayElementsOrNull(): Sequence<JsonNode>? =
        if (this.isArray) this.elements().asSequence() else null
}
