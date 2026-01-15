package com.edgerush.lootman.infrastructure.guild

import com.edgerush.lootman.domain.guild.model.BenchmarkMode
import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.model.GuildSettings
import com.edgerush.lootman.domain.guild.model.Region
import com.edgerush.lootman.domain.guild.model.SyncStatus
import com.edgerush.lootman.domain.guild.repository.GuildRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of GuildRepository.
 *
 * Persists Guild aggregates to the guild_configurations table.
 */
@Repository
class JdbcGuildRepository(
    private val jdbcTemplate: JdbcTemplate,
) : GuildRepository {
    override fun save(guild: Guild): Guild {
        val exists = existsById(guild.id)

        if (exists) {
            updateGuild(guild)
        } else {
            insertGuild(guild)
        }

        return guild
    }

    override fun findById(id: GuildId): Guild? {
        val sql =
            """
            SELECT guild_id, guild_name, guild_description, realm, region,
                   sync_enabled, sync_cron_expression, sync_run_on_startup,
                   last_sync_status, timezone, benchmark_mode,
                   custom_benchmark_rms, custom_benchmark_ipi,
                   is_active, created_at, updated_at
            FROM guild_configurations
            WHERE guild_id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, guildRowMapper, id.value)
        return results.firstOrNull()
    }

    override fun findAllActive(): List<Guild> {
        val sql =
            """
            SELECT guild_id, guild_name, guild_description, realm, region,
                   sync_enabled, sync_cron_expression, sync_run_on_startup,
                   last_sync_status, timezone, benchmark_mode,
                   custom_benchmark_rms, custom_benchmark_ipi,
                   is_active, created_at, updated_at
            FROM guild_configurations
            WHERE is_active = true
            ORDER BY guild_name
            """.trimIndent()

        return jdbcTemplate.query(sql, guildRowMapper)
    }

    override fun findAll(): List<Guild> {
        val sql =
            """
            SELECT guild_id, guild_name, guild_description, realm, region,
                   sync_enabled, sync_cron_expression, sync_run_on_startup,
                   last_sync_status, timezone, benchmark_mode,
                   custom_benchmark_rms, custom_benchmark_ipi,
                   is_active, created_at, updated_at
            FROM guild_configurations
            ORDER BY guild_name
            """.trimIndent()

        return jdbcTemplate.query(sql, guildRowMapper)
    }

    override fun deleteById(id: GuildId): Boolean {
        val sql = "DELETE FROM guild_configurations WHERE guild_id = ?"
        val rowsAffected = jdbcTemplate.update(sql, id.value)
        return rowsAffected > 0
    }

    override fun existsById(id: GuildId): Boolean {
        val sql = "SELECT COUNT(*) FROM guild_configurations WHERE guild_id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id.value) ?: 0
        return count > 0
    }

    private fun insertGuild(guild: Guild) {
        val sql =
            """
            INSERT INTO guild_configurations (
                guild_id, guild_name, guild_description, realm, region,
                sync_enabled, sync_cron_expression, sync_run_on_startup,
                last_sync_status, timezone, benchmark_mode,
                custom_benchmark_rms, custom_benchmark_ipi, is_active
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            guild.id.value,
            guild.name,
            guild.description,
            guild.realm,
            guild.region.name,
            guild.settings.syncEnabled,
            guild.settings.syncCronExpression,
            guild.settings.syncRunOnStartup,
            guild.syncStatus.name,
            guild.settings.timezone,
            guild.settings.benchmarkMode.name,
            guild.settings.customBenchmarkRms,
            guild.settings.customBenchmarkIpi,
            guild.isActive,
        )
    }

    private fun updateGuild(guild: Guild) {
        val sql =
            """
            UPDATE guild_configurations SET
                guild_name = ?,
                guild_description = ?,
                realm = ?,
                region = ?,
                sync_enabled = ?,
                sync_cron_expression = ?,
                sync_run_on_startup = ?,
                last_sync_status = ?,
                timezone = ?,
                benchmark_mode = ?,
                custom_benchmark_rms = ?,
                custom_benchmark_ipi = ?,
                is_active = ?,
                updated_at = NOW()
            WHERE guild_id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            guild.name,
            guild.description,
            guild.realm,
            guild.region.name,
            guild.settings.syncEnabled,
            guild.settings.syncCronExpression,
            guild.settings.syncRunOnStartup,
            guild.syncStatus.name,
            guild.settings.timezone,
            guild.settings.benchmarkMode.name,
            guild.settings.customBenchmarkRms,
            guild.settings.customBenchmarkIpi,
            guild.isActive,
            guild.id.value,
        )
    }

    private val guildRowMapper =
        RowMapper { rs, _ ->
            val customRms = rs.getDouble("custom_benchmark_rms")
            val customRmsValue = if (rs.wasNull()) null else customRms

            val customIpi = rs.getDouble("custom_benchmark_ipi")
            val customIpiValue = if (rs.wasNull()) null else customIpi

            val syncStatusStr = rs.getString("last_sync_status")
            val syncStatus =
                if (syncStatusStr != null) {
                    SyncStatus.fromString(syncStatusStr) ?: SyncStatus.NEVER_RUN
                } else {
                    SyncStatus.NEVER_RUN
                }

            val regionStr = rs.getString("region") ?: "US"
            val region = Region.fromString(regionStr) ?: Region.US

            val benchmarkModeStr = rs.getString("benchmark_mode") ?: "THEORETICAL"
            val benchmarkMode = BenchmarkMode.fromString(benchmarkModeStr) ?: BenchmarkMode.THEORETICAL

            Guild(
                id = GuildId(rs.getString("guild_id")),
                name = rs.getString("guild_name"),
                description = rs.getString("guild_description"),
                realm = rs.getString("realm"),
                region = region,
                settings =
                    GuildSettings(
                        syncEnabled = rs.getBoolean("sync_enabled"),
                        syncCronExpression = rs.getString("sync_cron_expression") ?: "0 0 4 * * *",
                        syncRunOnStartup = rs.getBoolean("sync_run_on_startup"),
                        timezone = rs.getString("timezone") ?: "UTC",
                        benchmarkMode = benchmarkMode,
                        customBenchmarkRms = customRmsValue,
                        customBenchmarkIpi = customIpiValue,
                    ),
                syncStatus = syncStatus,
                isActive = rs.getBoolean("is_active"),
                createdAt = rs.getTimestamp("created_at").toInstant(),
                updatedAt = rs.getTimestamp("updated_at").toInstant(),
            )
        }
}
