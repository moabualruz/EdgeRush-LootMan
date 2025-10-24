package com.edgerush.datasync.service

import com.edgerush.datasync.api.wowaudit.LootHistoryEntryResponse
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

@Service
class WoWAuditSyncService(
    private val client: WoWAuditClient,
    private val raiderService: RaiderService,
    private val lootAwardRepository: LootAwardRepository,
    private val wishlistSnapshotRepository: WishlistSnapshotRepository,
    private val attendanceStatRepository: AttendanceStatRepository,
    private val raidRepository: RaidRepository,
    private val raidSignupRepository: RaidSignupRepository,
    private val raidEncounterRepository: RaidEncounterRepository,
    private val historicalActivityRepository: HistoricalActivityRepository,
    private val guestRepository: GuestRepository,
    private val applicationRepository: ApplicationRepository,
    private val applicationAltRepository: ApplicationAltRepository,
    private val applicationQuestionRepository: ApplicationQuestionRepository,
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

    fun syncLootHistory(): Mono<Void> {
        val run = syncRunService.startRun("wowaudit-loot")
        return Mono.zip(fetchTeamInfo(), fetchCurrentPeriod())
            .flatMap { tuple ->
                val team = tuple.t1
                val period = tuple.t2
                val seasonId = period.seasonId
                if (seasonId == null) {
                    logger.warn("Season ID missing from period payload; skipping loot history sync")
                    return@flatMap Mono.just(0)
                }
                client.fetchLootHistory(team.guildId, team.id, seasonId)
                    .flatMap { body ->
                        Mono.fromCallable {
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
        return Mono.zip(fetchTeamInfoOptional(), client.fetchWishlists())
            .flatMapMany { tuple ->
                val team = tuple.t1
                val body = tuple.t2
                saveSnapshot("v1/wishlists", body)
                parseWishlistSummary(body, team).toFlux()
            }
            .flatMap({ summary ->
                client.fetchWishlistDetail(summary.id)
                    .map { payload -> summary to payload }
            }, 4)
            .flatMap { (summary, payload) ->
                Mono.fromCallable {
                    saveWishlistSnapshot(summary, payload)
                    summary
                }
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
                syncAttendanceData(team)
                    .then(syncRaidsData(team))
                    .then(syncHistoricalData(period))
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
            logger.warn("Unable to fetch WoWAudit team info: {}", ex.message)
            Mono.just(null)
        }

    private fun fetchTeamInfo(): Mono<TeamResponse> =
        client.fetchTeam().flatMap { body ->
            try {
                saveSnapshot("v1/team", body)
                Mono.just(objectMapper.readValue(body, TeamResponse::class.java))
            } catch (ex: Exception) {
                Mono.error(ex)
            }
        }

    private fun fetchCurrentPeriodOptional(): Mono<PeriodResponse?> =
        fetchCurrentPeriod().map { it }.onErrorResume { ex ->
            logger.warn("Unable to fetch WoWAudit period info: {}", ex.message)
            Mono.just(null)
        }

    private fun fetchCurrentPeriod(): Mono<PeriodResponse> =
        client.fetchPeriod().flatMap { body ->
            saveSnapshot("v1/period", body)
            parsePeriod(body)?.let { Mono.just(it) }
                ?: Mono.error(IllegalStateException("Missing season information"))
        }

    private fun parseRoster(body: String, team: TeamResponse?): List<RaiderRecord> {
        val defaultRegion = team?.region.orEmpty()
        val defaultRealm = team?.realm.orEmpty()
        val result = mutableListOf<RaiderRecord>()
        return try {
            val node = objectMapper.readTree(body)
            if (!node.isArray) {
                logger.warn("Unexpected roster payload format: {}", node.nodeType)
                emptyList()
            } else {
                node.forEach { element ->
                    val name = element.path("name").asText("")
                    if (name.isBlank()) return@forEach
                    val realm = element.path("realm").asText(defaultRealm)
                    val region = element.path("region").asText(defaultRegion)
                    val clazz = element.path("class").asText("")
                    val role = element.path("role").asText("")
                    val spec = element.path("spec").asText("")
                    result += RaiderRecord(
                        name = name,
                        realm = realm,
                        region = region,
                        clazz = clazz,
                        spec = spec,
                        role = role
                    )
                }
                result
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

    private fun processLootHistory(body: String, team: TeamResponse, period: PeriodResponse): Int =
        try {
            val root = objectMapper.readTree(body)
            val entriesNode: Iterable<JsonNode> = when {
                root.isArray -> root
                root.has("history_items") -> root["history_items"]
                root.has("loot") -> root["loot"]
                else -> {
                    logger.warn("Unexpected loot history payload format: {}", root.nodeType())
                    return 0
                }
            }
            var processed = 0
            for (node in entriesNode) {
                val entry = objectMapper.treeToValue(node, LootHistoryEntryResponse::class.java)
                val character = entry.character ?: continue
                val name = character.name?.takeIf { it.isNotBlank() } ?: continue
                val realm = character.realm?.takeIf { it.isNotBlank() } ?: team.realm.orEmpty()
                val region = character.region?.takeIf { it.isNotBlank() } ?: team.region.orEmpty()
                val raider = raiderService.upsertCharacter(
                    RaiderRecord(
                        name = name,
                        realm = realm,
                        region = region,
                        clazz = character.clazz.orEmpty(),
                        role = character.role.orEmpty()
                    )
                )
                val item = entry.item ?: continue
                val itemId = item.id ?: continue
                val itemName = item.name?.takeIf { it.isNotBlank() } ?: continue
                val tier = entry.tier.orEmpty()
                val awardedAt = entry.awardedAt?.let {
                    try {
                        OffsetDateTime.parse(it)
                    } catch (ex: Exception) {
                        logger.debug("Unable to parse awarded_at '{}': {}", it, ex.message)
                        OffsetDateTime.now()
                    }
                } ?: OffsetDateTime.now()
                val raiderId = raider.id ?: continue
                lootAwardRepository.save(
                    LootAwardEntity(
                        raiderId = raiderId,
                        itemId = itemId,
                        itemName = itemName,
                        tier = tier,
                        flps = 0.0,
                        rdf = 1.0,
                        awardedAt = awardedAt
                    )
                )
                processed++
            }
            processed
        } catch (ex: Exception) {
            logger.error("Failed to process loot history", ex)
            0
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
                else -> {
                    logger.warn("Unexpected wishlist summary payload format: {}", node.nodeType)
                    return emptyList()
                }
            }
            characters.forEach { element ->
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

    private fun saveWishlistSnapshot(summary: WishlistSummary, payload: String) {
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

        val raider = raiderService.upsertCharacter(
            RaiderRecord(
                name = name,
                realm = realm,
                region = region,
                clazz = clazz,
                role = role
            )
        )

        wishlistSnapshotRepository.deleteByCharacterNameAndCharacterRealm(name, realm)
        wishlistSnapshotRepository.save(
            WishlistSnapshotEntity(
                raiderId = raider.id,
                characterName = name,
                characterRealm = realm,
                rawPayload = payload,
                syncedAt = OffsetDateTime.now()
            )
        )
    }

    private fun syncAttendanceData(team: TeamResponse?): Mono<Unit> =
        client.fetchAttendance()
            .flatMap { body ->
                Mono.fromCallable {
                    saveSnapshot("v1/attendance", body)
                    val records = parseAttendance(body, team)
                    attendanceStatRepository.deleteAll()
                    attendanceStatRepository.saveAll(records.map { it.toEntity() }).forEach { }
                    logger.info("Synced {} attendance records", records.size)
                    Unit
                }
            }

    private fun syncRaidsData(team: TeamResponse?): Mono<Unit> {
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
                Mono.fromCallable { saveRaid(summary, detail) }
            }
            .count()
            .doOnNext { count -> logger.info("Synced {} raids from WoWAudit", count) }
            .thenReturn(Unit)
    }

    private fun syncHistoricalData(period: PeriodResponse?): Mono<Unit> {
        val periodId = period?.periodId ?: period?.currentPeriod
        if (periodId == null) {
            logger.warn("Period information unavailable; skipping historical activity sync")
            return Mono.empty()
        }
        return client.fetchHistoricalData(periodId)
            .flatMap { body ->
                Mono.fromCallable {
                    saveSnapshot("v1/historical_data?period=$periodId", body)
                    val records = parseHistoricalData(body, periodId)
                    historicalActivityRepository.deleteAll()
                    historicalActivityRepository.saveAll(records).forEach { }
                    logger.info("Synced {} historical activity records", records.size)
                    Unit
                }
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

    private fun parseAttendance(body: String, team: TeamResponse?): List<AttendanceRecord> {
        val defaultRealm = team?.realm.orEmpty()
        val defaultRegion = team?.region.orEmpty()
        val records = mutableListOf<AttendanceRecord>()
        return try {
            val node = objectMapper.readTree(body)
            val instance = node.path("instance").asText(null)
            val encounter = node.path("encounter").asText(null)
            val startDate = node.path("start_date").asText(null)
            val endDate = node.path("end_date").asText(null)
            val characters = node.path("characters")
            if (!characters.isArray) {
                logger.warn("Unexpected attendance payload format: {}", characters.nodeType())
                emptyList()
            } else {
                characters.forEach { element ->
                    val name = element.path("name").asText("")
                    if (name.isBlank()) return@forEach
                    val realm = element.path("realm").asText(defaultRealm)
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
                        characterClass = clazz,
                        characterRole = role,
                        attendedAmountOfRaids = element.path("attended_amount_of_raids").asIntOrNull(),
                        totalAmountOfRaids = element.path("total_amount_of_raids").asIntOrNull(),
                        attendedPercentage = element.path("attended_percentage").asDoubleOrNull(),
                        selectedAmountOfEncounters = element.path("selected_amount_of_encounters").asIntOrNull(),
                        totalAmountOfEncounters = element.path("total_amount_of_encounters").asIntOrNull(),
                        selectedPercentage = element.path("selected_percentage").asDoubleOrNull()
                    )
                }
                records
            }
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
                else -> {
                    logger.warn("Unexpected raids payload format: {}", node.nodeType)
                    return emptyList()
                }
            }
            raidsNode.forEach { element ->
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

    private fun saveRaid(summary: RaidSummary, detailBody: String) {
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
                    characterClass = character.path("class").asText(null),
                    characterRole = character.path("role").asText(null),
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

    private fun parseHistoricalData(body: String, periodId: Long): List<HistoricalActivityEntity> {
        val result = mutableListOf<HistoricalActivityEntity>()
        return try {
            val node = objectMapper.readTree(body)
            val characters = when {
                node.isArray -> node
                node.has("characters") -> node["characters"]
                else -> {
                    logger.warn("Unexpected historical data payload format: {}", node.nodeType)
                    return emptyList()
                }
            }
            characters.forEach { character ->
                val name = character.path("name").asText("")
                if (name.isBlank()) return@forEach
                val realm = character.path("realm").asText(null)
                val characterId = character.path("id").asLong(-1).takeIf { it > 0 }
                val dataNode = character.path("data")
                result += HistoricalActivityEntity(
                    characterId = characterId,
                    characterName = name,
                    characterRealm = realm,
                    period = periodId,
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
                logger.warn("Unexpected guests payload format: {}", node.nodeType)
                emptyList()
            } else {
                node.forEach { guestNode ->
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
            }
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
                else -> {
                    logger.warn("Unexpected applications payload format: {}", node.nodeType)
                    return emptyList()
                }
            }
            applications.forEach { app ->
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
                    mainCharacterName = main.path("name").asText(null),
                    mainCharacterRealm = main.path("realm").asText(null),
                    mainCharacterClass = main.path("class").asText(null),
                    mainCharacterRole = main.path("role").asText(null)
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
        applicationRepository.save(summary.toEntity())

        val altsNode = detailNode.path("alts")
        if (altsNode.isArray) {
            val alts = altsNode.map { alt ->
                ApplicationAltEntity(
                    applicationId = summary.id,
                    name = alt.path("name").asText(null),
                    realm = alt.path("realm").asText(null),
                    `class` = alt.path("class").asText(null),
                    role = alt.path("role").asText(null),
                    level = alt.path("level").asIntOrNull()
                )
            }
            applicationAltRepository.saveAll(alts).forEach { }
        }

        val questionsNode = detailNode.path("questions")
        if (questionsNode.isArray) {
            val questions = questionsNode.map { question ->
                val filesJson = if (question.has("files")) objectMapper.writeValueAsString(question["files"]) else null
                ApplicationQuestionEntity(
                    applicationId = summary.id,
                    question = question.path("question").asText(null),
                    answer = question.path("answer").asText(null),
                    filesJson = filesJson
                )
            }
            applicationQuestionRepository.saveAll(questions).forEach { }
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
}

data class RaiderRecord(
    val name: String,
    val realm: String,
    val region: String = "",
    val clazz: String = "",
    val spec: String = "",
    val role: String = ""
)

data class WishlistSummary(
    val id: Long,
    val name: String,
    val realm: String,
    val region: String
)

data class AttendanceRecord(
    val instance: String?,
    val encounter: String?,
    val startDate: String?,
    val endDate: String?,
    val characterId: Long?,
    val characterName: String,
    val characterRealm: String?,
    val characterClass: String?,
    val characterRole: String?,
    val attendedAmountOfRaids: Int?,
    val totalAmountOfRaids: Int?,
    val attendedPercentage: Double?,
    val selectedAmountOfEncounters: Int?,
    val totalAmountOfEncounters: Int?,
    val selectedPercentage: Double?
) {
    fun toEntity(): AttendanceStatEntity = AttendanceStatEntity(
        instance = instance,
        encounter = encounter,
        startDate = parseLocalDate(startDate),
        endDate = parseLocalDate(endDate),
        characterId = characterId,
        characterName = characterName,
        characterRealm = characterRealm,
        characterClass = characterClass,
        characterRole = characterRole,
        attendedAmountOfRaids = attendedAmountOfRaids,
        totalAmountOfRaids = totalAmountOfRaids,
        attendedPercentage = attendedPercentage,
        selectedAmountOfEncounters = selectedAmountOfEncounters,
        totalAmountOfEncounters = totalAmountOfEncounters,
        selectedPercentage = selectedPercentage,
        syncedAt = OffsetDateTime.now()
    )
}

data class RaidSummary(
    val id: Long,
    val date: String?,
    val startTime: String?,
    val endTime: String?,
    val instance: String?,
    val difficulty: String?,
    val optional: Boolean?,
    val status: String?,
    val presentSize: Int?,
    val totalSize: Int?,
    val notes: String?,
    val selectionsImage: String?
)

data class ApplicationSummary(
    val id: Long,
    val appliedAt: OffsetDateTime?,
    val status: String?,
    val role: String?,
    val age: Int?,
    val country: String?,
    val battletag: String?,
    val discordId: String?,
    val mainCharacterName: String?,
    val mainCharacterRealm: String?,
    val mainCharacterClass: String?,
    val mainCharacterRole: String?
) {
    fun toEntity(): ApplicationEntity = ApplicationEntity(
        applicationId = id,
        appliedAt = appliedAt,
        status = status,
        role = role,
        age = age,
        country = country,
        battletag = battletag,
        discordId = discordId,
        mainCharacterName = mainCharacterName,
        mainCharacterRealm = mainCharacterRealm,
        mainCharacterClass = mainCharacterClass,
        mainCharacterRole = mainCharacterRole,
        syncedAt = OffsetDateTime.now()
    )
}

private fun JsonNode.asIntOrNull(): Int? =
    if (this.isMissingNode || this.isNull) null else this.asInt()

private fun JsonNode.asDoubleOrNull(): Double? =
    if (this.isMissingNode || this.isNull) null else this.asDouble()

private fun JsonNode.asBooleanOrNull(): Boolean? =
    if (this.isMissingNode || this.isNull) null else this.asBoolean()

private fun parseLocalDate(value: String?): LocalDate? =
    value?.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    }

private fun parseLocalTime(value: String?): LocalTime? =
    value?.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalTime.parse(it) }.getOrNull()
    }

private fun parseOffsetDateTime(value: String?): OffsetDateTime? =
    value?.takeIf { it.isNotBlank() }?.let {
        runCatching { OffsetDateTime.parse(it) }.getOrNull()
    }
