package com.edgerush.lootman.infrastructure.application

import com.edgerush.lootman.domain.application.model.Application
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.application.model.ApplicationStatus
import com.edgerush.lootman.domain.application.repository.EnhancedApplicationRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp

/**
 * JDBC implementation of EnhancedApplicationRepository.
 *
 * Persists the Application domain model to the enhanced_applications table.
 * This supports the new recruitment system with OAuth and auto-fetch features.
 */
@Repository
class JdbcEnhancedApplicationRepository(
    private val jdbcTemplate: JdbcTemplate,
) : EnhancedApplicationRepository {

    private val objectMapper = ObjectMapper()

    override fun findById(id: ApplicationId): Application? {
        val sql = """
            SELECT enhanced_application_id, guild_id, battle_net_id, discord_id, email,
                   character_name, character_realm, character_class, specialization, item_level,
                   raider_io_score, best_parse_average, age, location, timezone,
                   raid_days_available, previous_guilds, reason_for_leaving, why_this_guild,
                   status, reviewed_by, reviewed_at, created_at, updated_at
            FROM enhanced_applications
            WHERE enhanced_application_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, applicationRowMapper, id.value).firstOrNull()
    }

    override fun findByGuildId(guildId: GuildId, offset: Long, limit: Int): List<Application> {
        val sql = """
            SELECT enhanced_application_id, guild_id, battle_net_id, discord_id, email,
                   character_name, character_realm, character_class, specialization, item_level,
                   raider_io_score, best_parse_average, age, location, timezone,
                   raid_days_available, previous_guilds, reason_for_leaving, why_this_guild,
                   status, reviewed_by, reviewed_at, created_at, updated_at
            FROM enhanced_applications
            WHERE guild_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, applicationRowMapper, guildId.value, limit, offset)
    }

    override fun findByGuildIdAndStatus(
        guildId: GuildId,
        status: ApplicationStatus,
        offset: Long,
        limit: Int,
    ): List<Application> {
        val sql = """
            SELECT enhanced_application_id, guild_id, battle_net_id, discord_id, email,
                   character_name, character_realm, character_class, specialization, item_level,
                   raider_io_score, best_parse_average, age, location, timezone,
                   raid_days_available, previous_guilds, reason_for_leaving, why_this_guild,
                   status, reviewed_by, reviewed_at, created_at, updated_at
            FROM enhanced_applications
            WHERE guild_id = ? AND status = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, applicationRowMapper, guildId.value, status.name, limit, offset)
    }

    override fun findByGuildIdAndDiscordId(guildId: GuildId, discordId: String): Application? {
        val sql = """
            SELECT enhanced_application_id, guild_id, battle_net_id, discord_id, email,
                   character_name, character_realm, character_class, specialization, item_level,
                   raider_io_score, best_parse_average, age, location, timezone,
                   raid_days_available, previous_guilds, reason_for_leaving, why_this_guild,
                   status, reviewed_by, reviewed_at, created_at, updated_at
            FROM enhanced_applications
            WHERE guild_id = ? AND discord_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, applicationRowMapper, guildId.value, discordId).firstOrNull()
    }

    override fun findByGuildIdAndBattleNetId(guildId: GuildId, battleNetId: String): Application? {
        val sql = """
            SELECT enhanced_application_id, guild_id, battle_net_id, discord_id, email,
                   character_name, character_realm, character_class, specialization, item_level,
                   raider_io_score, best_parse_average, age, location, timezone,
                   raid_days_available, previous_guilds, reason_for_leaving, why_this_guild,
                   status, reviewed_by, reviewed_at, created_at, updated_at
            FROM enhanced_applications
            WHERE guild_id = ? AND battle_net_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, applicationRowMapper, guildId.value, battleNetId).firstOrNull()
    }

    override fun countByGuildId(guildId: GuildId): Long {
        val sql = "SELECT COUNT(*) FROM enhanced_applications WHERE guild_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId.value) ?: 0L
    }

    override fun countByGuildIdAndStatus(guildId: GuildId, status: ApplicationStatus): Long {
        val sql = "SELECT COUNT(*) FROM enhanced_applications WHERE guild_id = ? AND status = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId.value, status.name) ?: 0L
    }

    override fun existsById(id: ApplicationId): Boolean {
        val sql = "SELECT COUNT(*) FROM enhanced_applications WHERE enhanced_application_id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id.value) ?: 0
        return count > 0
    }

    override fun save(application: Application): Application {
        return if (existsById(application.id)) {
            update(application)
        } else {
            insert(application)
        }
    }

    override fun deleteById(id: ApplicationId) {
        val sql = "DELETE FROM enhanced_applications WHERE enhanced_application_id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    private fun insert(application: Application): Application {
        val sql = """
            INSERT INTO enhanced_applications (
                enhanced_application_id, guild_id, battle_net_id, discord_id, email,
                character_name, character_realm, character_class, specialization, item_level,
                raider_io_score, best_parse_average, age, location, timezone,
                raid_days_available, previous_guilds, reason_for_leaving, why_this_guild,
                status, reviewed_by, reviewed_at, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            application.id.value,
            application.guildId.value,
            application.battleNetId,
            application.discordId,
            application.email,
            application.characterName,
            application.characterRealm,
            application.characterClass,
            application.specialization,
            application.itemLevel,
            application.raiderIOScore,
            application.bestParseAverage,
            application.age,
            application.location,
            application.timezone,
            objectMapper.writeValueAsString(application.raidDaysAvailable),
            application.previousGuilds,
            application.reasonForLeaving,
            application.whyThisGuild,
            application.status.name,
            application.reviewedBy,
            application.reviewedAt?.let { Timestamp.from(it) },
            Timestamp.from(application.createdAt),
            Timestamp.from(application.updatedAt),
        )

        return application
    }

    private fun update(application: Application): Application {
        val sql = """
            UPDATE enhanced_applications SET
                guild_id = ?, battle_net_id = ?, discord_id = ?, email = ?,
                character_name = ?, character_realm = ?, character_class = ?, specialization = ?,
                item_level = ?, raider_io_score = ?, best_parse_average = ?,
                age = ?, location = ?, timezone = ?, raid_days_available = ?,
                previous_guilds = ?, reason_for_leaving = ?, why_this_guild = ?,
                status = ?, reviewed_by = ?, reviewed_at = ?, updated_at = ?
            WHERE enhanced_application_id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            application.guildId.value,
            application.battleNetId,
            application.discordId,
            application.email,
            application.characterName,
            application.characterRealm,
            application.characterClass,
            application.specialization,
            application.itemLevel,
            application.raiderIOScore,
            application.bestParseAverage,
            application.age,
            application.location,
            application.timezone,
            objectMapper.writeValueAsString(application.raidDaysAvailable),
            application.previousGuilds,
            application.reasonForLeaving,
            application.whyThisGuild,
            application.status.name,
            application.reviewedBy,
            application.reviewedAt?.let { Timestamp.from(it) },
            Timestamp.from(application.updatedAt),
            application.id.value,
        )

        return application
    }

    private val applicationRowMapper = RowMapper { rs, _ ->
        fun getDoubleOrNull(col: String): Double? {
            val value = rs.getDouble(col)
            return if (rs.wasNull()) null else value
        }

        val raidDaysJson = rs.getString("raid_days_available")
        val raidDays: List<String> = try {
            objectMapper.readValue(raidDaysJson, object : TypeReference<List<String>>() {})
        } catch (e: Exception) {
            emptyList()
        }

        Application.reconstruct(
            id = ApplicationId(rs.getString("enhanced_application_id")),
            guildId = GuildId(rs.getString("guild_id")),
            battleNetId = rs.getString("battle_net_id"),
            discordId = rs.getString("discord_id"),
            email = rs.getString("email"),
            characterName = rs.getString("character_name"),
            characterRealm = rs.getString("character_realm"),
            characterClass = rs.getString("character_class"),
            specialization = rs.getString("specialization"),
            itemLevel = rs.getDouble("item_level"),
            raiderIOScore = getDoubleOrNull("raider_io_score"),
            bestParseAverage = getDoubleOrNull("best_parse_average"),
            age = rs.getInt("age"),
            location = rs.getString("location"),
            timezone = rs.getString("timezone"),
            raidDaysAvailable = raidDays,
            previousGuilds = rs.getString("previous_guilds"),
            reasonForLeaving = rs.getString("reason_for_leaving"),
            whyThisGuild = rs.getString("why_this_guild"),
            status = ApplicationStatus.valueOf(rs.getString("status")),
            reviewedBy = rs.getString("reviewed_by"),
            reviewedAt = rs.getTimestamp("reviewed_at")?.toInstant(),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at").toInstant(),
        )
    }
}
