package com.edgerush.lootman.infrastructure.guild

import com.edgerush.datasync.entity.GuildConfigurationEntity
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * JDBC implementation of GuildConfigurationRepository.
 *
 * Persists GuildConfigurationEntity to the guild_configurations table.
 */
@Repository
class JdbcGuildConfigurationRepository(
    private val jdbcTemplate: JdbcTemplate,
) : GuildConfigurationRepository {
    override fun findById(id: Long): GuildConfigurationEntity? {
        val sql =
            """
            SELECT id, guild_id, guild_name, guild_description, wowaudit_api_key_encrypted,
                   wowaudit_guild_uri, wowaudit_base_url, sync_enabled, sync_cron_expression,
                   sync_run_on_startup, last_sync_at, last_sync_status, last_sync_error,
                   timezone, is_active, created_at, updated_at, benchmark_mode,
                   custom_benchmark_rms, custom_benchmark_ipi, benchmark_updated_at,
                   bnet_realm_slug, bnet_guild_name_slug, bnet_region,
                   bnet_last_sync_at, bnet_last_sync_status, bnet_last_sync_error, bnet_sync_enabled
            FROM guild_configurations
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, guildConfigurationRowMapper, id)
        return results.firstOrNull()
    }

    override fun findByGuildId(guildId: String): GuildConfigurationEntity? {
        val sql =
            """
            SELECT id, guild_id, guild_name, guild_description, wowaudit_api_key_encrypted,
                   wowaudit_guild_uri, wowaudit_base_url, sync_enabled, sync_cron_expression,
                   sync_run_on_startup, last_sync_at, last_sync_status, last_sync_error,
                   timezone, is_active, created_at, updated_at, benchmark_mode,
                   custom_benchmark_rms, custom_benchmark_ipi, benchmark_updated_at,
                   bnet_realm_slug, bnet_guild_name_slug, bnet_region,
                   bnet_last_sync_at, bnet_last_sync_status, bnet_last_sync_error, bnet_sync_enabled
            FROM guild_configurations
            WHERE guild_id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, guildConfigurationRowMapper, guildId)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM guild_configurations WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<GuildConfigurationEntity> {
        val sql =
            """
            SELECT id, guild_id, guild_name, guild_description, wowaudit_api_key_encrypted,
                   wowaudit_guild_uri, wowaudit_base_url, sync_enabled, sync_cron_expression,
                   sync_run_on_startup, last_sync_at, last_sync_status, last_sync_error,
                   timezone, is_active, created_at, updated_at, benchmark_mode,
                   custom_benchmark_rms, custom_benchmark_ipi, benchmark_updated_at,
                   bnet_realm_slug, bnet_guild_name_slug, bnet_region,
                   bnet_last_sync_at, bnet_last_sync_status, bnet_last_sync_error, bnet_sync_enabled
            FROM guild_configurations
            ORDER BY guild_name, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, guildConfigurationRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM guild_configurations"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findActive(
        offset: Long,
        limit: Int,
    ): List<GuildConfigurationEntity> {
        val sql =
            """
            SELECT id, guild_id, guild_name, guild_description, wowaudit_api_key_encrypted,
                   wowaudit_guild_uri, wowaudit_base_url, sync_enabled, sync_cron_expression,
                   sync_run_on_startup, last_sync_at, last_sync_status, last_sync_error,
                   timezone, is_active, created_at, updated_at, benchmark_mode,
                   custom_benchmark_rms, custom_benchmark_ipi, benchmark_updated_at,
                   bnet_realm_slug, bnet_guild_name_slug, bnet_region,
                   bnet_last_sync_at, bnet_last_sync_status, bnet_last_sync_error, bnet_sync_enabled
            FROM guild_configurations
            WHERE is_active = true
            ORDER BY guild_name, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, guildConfigurationRowMapper, limit, offset)
    }

    override fun countActive(): Long {
        val sql = "SELECT COUNT(*) FROM guild_configurations WHERE is_active = true"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun save(entity: GuildConfigurationEntity): GuildConfigurationEntity {
        return if (entity.id == null) {
            insertGuildConfiguration(entity)
        } else {
            updateGuildConfiguration(entity)
            entity
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM guild_configurations WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertGuildConfiguration(entity: GuildConfigurationEntity): GuildConfigurationEntity {
        val sql =
            """
            INSERT INTO guild_configurations (
                guild_id, guild_name, guild_description, wowaudit_api_key_encrypted,
                wowaudit_guild_uri, wowaudit_base_url, sync_enabled, sync_cron_expression,
                sync_run_on_startup, last_sync_at, last_sync_status, last_sync_error,
                timezone, is_active, created_at, updated_at, benchmark_mode,
                custom_benchmark_rms, custom_benchmark_ipi, benchmark_updated_at,
                bnet_realm_slug, bnet_guild_name_slug, bnet_region,
                bnet_last_sync_at, bnet_last_sync_status, bnet_last_sync_error, bnet_sync_enabled
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, entity.guildId)
            ps.setString(2, entity.guildName)
            entity.guildDescription?.let { ps.setString(3, it) } ?: ps.setNull(3, java.sql.Types.VARCHAR)
            entity.wowauditApiKeyEncrypted?.let { ps.setString(4, it) } ?: ps.setNull(4, java.sql.Types.VARCHAR)
            entity.wowauditGuildUri?.let { ps.setString(5, it) } ?: ps.setNull(5, java.sql.Types.VARCHAR)
            ps.setString(6, entity.wowauditBaseUrl)
            ps.setBoolean(7, entity.syncEnabled)
            ps.setString(8, entity.syncCronExpression)
            ps.setBoolean(9, entity.syncRunOnStartup)
            entity.lastSyncAt?.let { ps.setTimestamp(10, Timestamp.from(it.toInstant())) } ?: ps.setNull(10, java.sql.Types.TIMESTAMP)
            entity.lastSyncStatus?.let { ps.setString(11, it) } ?: ps.setNull(11, java.sql.Types.VARCHAR)
            entity.lastSyncError?.let { ps.setString(12, it) } ?: ps.setNull(12, java.sql.Types.VARCHAR)
            ps.setString(13, entity.timezone)
            ps.setBoolean(14, entity.isActive)
            ps.setTimestamp(15, Timestamp.from(entity.createdAt.toInstant()))
            ps.setTimestamp(16, Timestamp.from(entity.updatedAt.toInstant()))
            ps.setString(17, entity.benchmarkMode)
            entity.customBenchmarkRms?.let { ps.setBigDecimal(18, it) } ?: ps.setNull(18, java.sql.Types.DECIMAL)
            entity.customBenchmarkIpi?.let { ps.setBigDecimal(19, it) } ?: ps.setNull(19, java.sql.Types.DECIMAL)
            entity.benchmarkUpdatedAt?.let { ps.setTimestamp(20, Timestamp.from(it.toInstant())) } ?: ps.setNull(20, java.sql.Types.TIMESTAMP)
            entity.bnetRealmSlug?.let { ps.setString(21, it) } ?: ps.setNull(21, java.sql.Types.VARCHAR)
            entity.bnetGuildNameSlug?.let { ps.setString(22, it) } ?: ps.setNull(22, java.sql.Types.VARCHAR)
            ps.setString(23, entity.bnetRegion)
            entity.bnetLastSyncAt?.let { ps.setTimestamp(24, Timestamp.from(it.toInstant())) } ?: ps.setNull(24, java.sql.Types.TIMESTAMP)
            entity.bnetLastSyncStatus?.let { ps.setString(25, it) } ?: ps.setNull(25, java.sql.Types.VARCHAR)
            entity.bnetLastSyncError?.let { ps.setString(26, it) } ?: ps.setNull(26, java.sql.Types.VARCHAR)
            ps.setBoolean(27, entity.bnetSyncEnabled)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return entity.copy(id = generatedId?.toLong())
    }

    private fun updateGuildConfiguration(entity: GuildConfigurationEntity) {
        val sql =
            """
            UPDATE guild_configurations SET
                guild_id = ?,
                guild_name = ?,
                guild_description = ?,
                wowaudit_api_key_encrypted = ?,
                wowaudit_guild_uri = ?,
                wowaudit_base_url = ?,
                sync_enabled = ?,
                sync_cron_expression = ?,
                sync_run_on_startup = ?,
                last_sync_at = ?,
                last_sync_status = ?,
                last_sync_error = ?,
                timezone = ?,
                is_active = ?,
                updated_at = ?,
                benchmark_mode = ?,
                custom_benchmark_rms = ?,
                custom_benchmark_ipi = ?,
                benchmark_updated_at = ?,
                bnet_realm_slug = ?,
                bnet_guild_name_slug = ?,
                bnet_region = ?,
                bnet_last_sync_at = ?,
                bnet_last_sync_status = ?,
                bnet_last_sync_error = ?,
                bnet_sync_enabled = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.guildId,
            entity.guildName,
            entity.guildDescription,
            entity.wowauditApiKeyEncrypted,
            entity.wowauditGuildUri,
            entity.wowauditBaseUrl,
            entity.syncEnabled,
            entity.syncCronExpression,
            entity.syncRunOnStartup,
            entity.lastSyncAt?.let { Timestamp.from(it.toInstant()) },
            entity.lastSyncStatus,
            entity.lastSyncError,
            entity.timezone,
            entity.isActive,
            Timestamp.from(OffsetDateTime.now().toInstant()),
            entity.benchmarkMode,
            entity.customBenchmarkRms,
            entity.customBenchmarkIpi,
            entity.benchmarkUpdatedAt?.let { Timestamp.from(it.toInstant()) },
            entity.bnetRealmSlug,
            entity.bnetGuildNameSlug,
            entity.bnetRegion,
            entity.bnetLastSyncAt?.let { Timestamp.from(it.toInstant()) },
            entity.bnetLastSyncStatus,
            entity.bnetLastSyncError,
            entity.bnetSyncEnabled,
            entity.id,
        )
    }

    private val guildConfigurationRowMapper =
        RowMapper { rs, _ ->
            GuildConfigurationEntity(
                id = rs.getLong("id"),
                guildId = rs.getString("guild_id"),
                guildName = rs.getString("guild_name"),
                guildDescription = rs.getString("guild_description"),
                wowauditApiKeyEncrypted = rs.getString("wowaudit_api_key_encrypted"),
                wowauditGuildUri = rs.getString("wowaudit_guild_uri"),
                wowauditBaseUrl = rs.getString("wowaudit_base_url") ?: "https://wowaudit.com",
                syncEnabled = rs.getBoolean("sync_enabled"),
                syncCronExpression = rs.getString("sync_cron_expression") ?: "0 0 4 * * *",
                syncRunOnStartup = rs.getBoolean("sync_run_on_startup"),
                lastSyncAt = rs.getTimestamp("last_sync_at")?.toInstant()?.atOffset(ZoneOffset.UTC),
                lastSyncStatus = rs.getString("last_sync_status"),
                lastSyncError = rs.getString("last_sync_error"),
                timezone = rs.getString("timezone") ?: "UTC",
                isActive = rs.getBoolean("is_active"),
                createdAt = rs.getTimestamp("created_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
                updatedAt = rs.getTimestamp("updated_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
                benchmarkMode = rs.getString("benchmark_mode") ?: "THEORETICAL",
                customBenchmarkRms = rs.getBigDecimal("custom_benchmark_rms"),
                customBenchmarkIpi = rs.getBigDecimal("custom_benchmark_ipi"),
                benchmarkUpdatedAt = rs.getTimestamp("benchmark_updated_at")?.toInstant()?.atOffset(ZoneOffset.UTC),
                bnetRealmSlug = rs.getString("bnet_realm_slug"),
                bnetGuildNameSlug = rs.getString("bnet_guild_name_slug"),
                bnetRegion = rs.getString("bnet_region") ?: "eu",
                bnetLastSyncAt = rs.getTimestamp("bnet_last_sync_at")?.toInstant()?.atOffset(ZoneOffset.UTC),
                bnetLastSyncStatus = rs.getString("bnet_last_sync_status"),
                bnetLastSyncError = rs.getString("bnet_last_sync_error"),
                bnetSyncEnabled = rs.getBoolean("bnet_sync_enabled"),
            )
        }
}
