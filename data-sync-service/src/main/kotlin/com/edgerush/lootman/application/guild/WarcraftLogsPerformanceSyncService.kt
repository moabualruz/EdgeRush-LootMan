package com.edgerush.lootman.application.guild

import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.lootman.domain.application.client.GuildReportData
import com.edgerush.lootman.domain.application.client.WarcraftLogsClient
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.sync.repository.SyncRunRepository
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime

/**
 * Syncs guild report performance data from Warcraft Logs.
 *
 * Populates warcraft_logs_reports, warcraft_logs_fights, and warcraft_logs_performance
 * tables used by JdbcRaiderPerformanceRepository for MAS (Mechanical Adherence Score) calculation.
 */
@Service
class WarcraftLogsPerformanceSyncService(
    private val warcraftLogsClient: WarcraftLogsClient,
    private val guildConfigurationRepository: GuildConfigurationRepository,
    private val syncRunRepository: SyncRunRepository,
    private val jdbcTemplate: JdbcTemplate,
) {
    private val logger = LoggerFactory.getLogger(WarcraftLogsPerformanceSyncService::class.java)

    /**
     * Sync performance data for a guild from Warcraft Logs.
     *
     * Fetches recent guild reports, extracts fight data and player participation,
     * and persists into the warcraft_logs_* tables.
     *
     * @param guildId The guild identifier
     * @param reportLimit Maximum number of reports to fetch (default 10)
     * @return Mono containing the sync result
     */
    fun syncPerformanceData(
        guildId: String,
        reportLimit: Int = 10,
    ): Mono<PerformanceSyncResult> {
        val guildConfig =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: return Mono.error(IllegalArgumentException("Guild configuration not found for guildId=$guildId"))

        val guildName = guildConfig.bnetGuildNameSlug ?: guildConfig.guildName
        ?: return Mono.error(IllegalArgumentException("Guild name not configured for guildId=$guildId"))

        val realm = guildConfig.bnetRealmSlug ?: return Mono.error(
            IllegalArgumentException("Realm not configured for guildId=$guildId. Set bnetRealmSlug in guild configuration."),
        )
        val region = guildConfig.bnetRegion

        logger.info("Starting WarcraftLogs performance sync for guild={} ({}-{}-{})", guildId, guildName, realm, region)

        // Start sync run log
        val syncRun =
            syncRunRepository.save(
                SyncRunEntity(
                    source = "WarcraftLogs-Performance",
                    status = "RUNNING",
                    startedAt = OffsetDateTime.now(),
                    completedAt = null,
                    message = "Starting performance sync for guild $guildId",
                ),
            )

        return warcraftLogsClient.fetchGuildReports(
            guildName = guildName,
            serverSlug = realm,
            serverRegion = region,
            limit = reportLimit,
        ).map { reports ->
            val result = persistReports(guildId, reports)

            // Complete sync run
            syncRunRepository.save(
                syncRun.copy(
                    status = "COMPLETED",
                    completedAt = OffsetDateTime.now(),
                    message = "Synced ${result.reportsInserted} reports, ${result.fightsInserted} fights, ${result.performanceRowsInserted} performance rows",
                ),
            )

            logger.info(
                "WarcraftLogs performance sync completed for guild={}: reports={}, fights={}, performance={}",
                guildId,
                result.reportsInserted,
                result.fightsInserted,
                result.performanceRowsInserted,
            )

            result
        }.onErrorResume { e ->
            logger.error("WarcraftLogs performance sync failed for guild={}: {}", guildId, e.message, e)

            syncRunRepository.save(
                syncRun.copy(
                    status = "FAILED",
                    completedAt = OffsetDateTime.now(),
                    message = "Failed: ${e.message}",
                ),
            )

            Mono.just(PerformanceSyncResult(error = e.message))
        }
    }

    /**
     * Persist reports, fights, and performance data into the database.
     *
     * Uses INSERT ... ON CONFLICT DO NOTHING for reports (dedup by report_code)
     * and INSERT ... ON CONFLICT DO NOTHING for fights (dedup by report_id + fight_id).
     */
    internal fun persistReports(
        guildId: String,
        reports: List<GuildReportData>,
    ): PerformanceSyncResult {
        var reportsInserted = 0
        var fightsInserted = 0
        var performanceRows = 0

        for (report in reports) {
            if (report.reportCode.isBlank()) continue

            // Upsert report
            val reportId = upsertReport(guildId, report)
            if (reportId != null) {
                reportsInserted++

                // Insert fights
                for (fight in report.fights) {
                    val fightDbId = upsertFight(reportId, fight.fightId, fight)
                    if (fightDbId != null) {
                        fightsInserted++

                        // Insert player performance rows for this fight
                        // WCL fights API returns friendlyPlayers (list of player IDs)
                        // but we don't have names from this query alone.
                        // Use the raider roster to map names to this fight.
                        // For now, insert a placeholder row per fight with the encounter data
                        // so MAS can at minimum count fights.
                        performanceRows += insertFightPerformance(fightDbId, guildId, report, fight)
                    }
                }
            }
        }

        return PerformanceSyncResult(
            reportsInserted = reportsInserted,
            fightsInserted = fightsInserted,
            performanceRowsInserted = performanceRows,
        )
    }

    /**
     * Insert or get existing report. Returns the DB id.
     */
    private fun upsertReport(
        guildId: String,
        report: GuildReportData,
    ): Long? {
        // Check if report already exists
        val existingId = jdbcTemplate.query(
            "SELECT id FROM warcraft_logs_reports WHERE report_code = ?",
            { rs, _ -> rs.getLong("id") },
            report.reportCode,
        ).firstOrNull()

        if (existingId != null) {
            logger.debug("Report {} already exists with id={}", report.reportCode, existingId)
            return existingId
        }

        return try {
            jdbcTemplate.queryForObject(
                """
                INSERT INTO warcraft_logs_reports (guild_id, report_code, title, owner, start_time, end_time, zone_name, synced_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
                ON CONFLICT (report_code) DO NOTHING
                RETURNING id
                """.trimIndent(),
                Long::class.java,
                guildId,
                report.reportCode,
                report.title,
                report.owner,
                Timestamp.from(Instant.ofEpochMilli(report.startTime)),
                Timestamp.from(Instant.ofEpochMilli(report.endTime)),
                report.zone,
            )
        } catch (e: Exception) {
            logger.warn("Failed to insert report {}: {}", report.reportCode, e.message)
            // Might have been inserted by concurrent process, try to fetch
            jdbcTemplate.query(
                "SELECT id FROM warcraft_logs_reports WHERE report_code = ?",
                { rs, _ -> rs.getLong("id") },
                report.reportCode,
            ).firstOrNull()
        }
    }

    /**
     * Insert or get existing fight. Returns the DB id.
     */
    private fun upsertFight(
        reportId: Long,
        fightId: Int,
        fight: com.edgerush.lootman.domain.application.client.GuildReportFight,
    ): Long? {
        // Check if fight already exists
        val existingId = jdbcTemplate.query(
            "SELECT id FROM warcraft_logs_fights WHERE report_id = ? AND fight_id = ?",
            { rs, _ -> rs.getLong("id") },
            reportId,
            fightId,
        ).firstOrNull()

        if (existingId != null) return existingId

        return try {
            jdbcTemplate.queryForObject(
                """
                INSERT INTO warcraft_logs_fights (report_id, fight_id, encounter_id, encounter_name, difficulty, kill, start_time, end_time, boss_percentage)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (report_id, fight_id) DO NOTHING
                RETURNING id
                """.trimIndent(),
                Long::class.java,
                reportId,
                fightId,
                fight.encounterId,
                fight.encounterName,
                fight.difficulty,
                fight.kill,
                // Fight times are offsets from report start, store as absolute timestamps
                null as Timestamp?, // start_time - we'd need report start time
                null as Timestamp?, // end_time
                fight.bossPercentage,
            )
        } catch (e: Exception) {
            logger.warn("Failed to insert fight {} for report {}: {}", fightId, reportId, e.message)
            jdbcTemplate.query(
                "SELECT id FROM warcraft_logs_fights WHERE report_id = ? AND fight_id = ?",
                { rs, _ -> rs.getLong("id") },
                reportId,
                fightId,
            ).firstOrNull()
        }
    }

    /**
     * Insert performance rows for a fight.
     *
     * Uses the guild's raider roster to create performance entries for each raider
     * who participated in the fight. Deaths count defaults to 0 — a future iteration
     * can query the WCL events API for death data.
     *
     * @return Number of rows inserted
     */
    private fun insertFightPerformance(
        fightDbId: Long,
        guildId: String,
        report: GuildReportData,
        fight: com.edgerush.lootman.domain.application.client.GuildReportFight,
    ): Int {
        // If playerDetails are available from the API response, use those
        if (fight.playerDetails.isNotEmpty()) {
            var count = 0
            for (player in fight.playerDetails) {
                if (player.name.isBlank()) continue
                try {
                    jdbcTemplate.update(
                        """
                        INSERT INTO warcraft_logs_performance (fight_id, character_name, character_realm, character_class, spec, deaths, avoidable_damage_percentage)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        fightDbId,
                        player.name,
                        player.server,
                        player.type,
                        player.spec,
                        player.deaths,
                        0.0, // Avoidable damage not available from fights API
                    )
                    count++
                } catch (e: Exception) {
                    logger.debug("Skip duplicate performance row for {} in fight {}", player.name, fightDbId)
                }
            }
            return count
        }

        // Fallback: Insert guild raiders as participants (from raider roster)
        // This gives us basic fight count data for MAS even without per-fight death data
        val raiders = jdbcTemplate.query(
            "SELECT character_name, realm FROM raiders WHERE guild_id = ?",
            { rs, _ -> Pair(rs.getString("character_name"), rs.getString("realm")) },
            guildId,
        )

        var count = 0
        for ((name, realm) in raiders) {
            try {
                jdbcTemplate.update(
                    """
                    INSERT INTO warcraft_logs_performance (fight_id, character_name, character_realm, character_class, spec, deaths, avoidable_damage_percentage)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT DO NOTHING
                    """.trimIndent(),
                    fightDbId,
                    name,
                    realm,
                    null, // class unknown without additional API call
                    null, // spec unknown
                    0,    // deaths default to 0
                    0.0,  // avoidable damage default to 0
                )
                count++
            } catch (e: Exception) {
                logger.debug("Skip duplicate performance row for {} in fight {}", name, fightDbId)
            }
        }
        return count
    }
}

/**
 * Result of a performance data sync.
 */
data class PerformanceSyncResult(
    val reportsInserted: Int = 0,
    val fightsInserted: Int = 0,
    val performanceRowsInserted: Int = 0,
    val error: String? = null,
) {
    val success: Boolean get() = error == null
}
