package com.edgerush.lootman.infrastructure.team

import com.edgerush.datasync.entity.TeamMetadataEntity
import com.edgerush.lootman.domain.team.repository.TeamMetadataRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * JDBC implementation of TeamMetadataRepository.
 *
 * Persists TeamMetadataEntity to the team_metadata table.
 */
@Repository
class JdbcTeamMetadataRepository(
    private val jdbcTemplate: JdbcTemplate,
) : TeamMetadataRepository {
    override fun findById(teamId: Long): TeamMetadataEntity? {
        val sql =
            """
            SELECT team_id, guild_id, guild_name, name, region, realm, url,
                   last_refreshed_blizzard, last_refreshed_percentiles, last_refreshed_mythic_plus,
                   wishlist_updated_at, synced_at
            FROM team_metadata
            WHERE team_id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, teamMetadataRowMapper, teamId)
        return results.firstOrNull()
    }

    override fun existsById(teamId: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM team_metadata WHERE team_id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, teamId) ?: 0
        return count > 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<TeamMetadataEntity> {
        val sql =
            """
            SELECT team_id, guild_id, guild_name, name, region, realm, url,
                   last_refreshed_blizzard, last_refreshed_percentiles, last_refreshed_mythic_plus,
                   wishlist_updated_at, synced_at
            FROM team_metadata
            ORDER BY synced_at DESC, team_id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, teamMetadataRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM team_metadata"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByGuildId(
        guildId: Long,
        offset: Long,
        limit: Int,
    ): List<TeamMetadataEntity> {
        val sql =
            """
            SELECT team_id, guild_id, guild_name, name, region, realm, url,
                   last_refreshed_blizzard, last_refreshed_percentiles, last_refreshed_mythic_plus,
                   wishlist_updated_at, synced_at
            FROM team_metadata
            WHERE guild_id = ?
            ORDER BY synced_at DESC, team_id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, teamMetadataRowMapper, guildId, limit, offset)
    }

    override fun countByGuildId(guildId: Long): Long {
        val sql = "SELECT COUNT(*) FROM team_metadata WHERE guild_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId) ?: 0L
    }

    override fun findByRegion(
        region: String,
        offset: Long,
        limit: Int,
    ): List<TeamMetadataEntity> {
        val sql =
            """
            SELECT team_id, guild_id, guild_name, name, region, realm, url,
                   last_refreshed_blizzard, last_refreshed_percentiles, last_refreshed_mythic_plus,
                   wishlist_updated_at, synced_at
            FROM team_metadata
            WHERE region = ?
            ORDER BY synced_at DESC, team_id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, teamMetadataRowMapper, region, limit, offset)
    }

    override fun countByRegion(region: String): Long {
        val sql = "SELECT COUNT(*) FROM team_metadata WHERE region = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, region) ?: 0L
    }

    override fun save(entity: TeamMetadataEntity): TeamMetadataEntity {
        return if (existsById(entity.teamId)) {
            updateTeamMetadata(entity)
            entity
        } else {
            insertTeamMetadata(entity)
        }
    }

    override fun delete(teamId: Long) {
        val sql = "DELETE FROM team_metadata WHERE team_id = ?"
        jdbcTemplate.update(sql, teamId)
    }

    private fun insertTeamMetadata(entity: TeamMetadataEntity): TeamMetadataEntity {
        val sql =
            """
            INSERT INTO team_metadata (
                team_id, guild_id, guild_name, name, region, realm, url,
                last_refreshed_blizzard, last_refreshed_percentiles, last_refreshed_mythic_plus,
                wishlist_updated_at, synced_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.teamId,
            entity.guildId,
            entity.guildName,
            entity.name,
            entity.region,
            entity.realm,
            entity.url,
            entity.lastRefreshedBlizzard?.let { Timestamp.from(it.toInstant()) },
            entity.lastRefreshedPercentiles?.let { Timestamp.from(it.toInstant()) },
            entity.lastRefreshedMythicPlus?.let { Timestamp.from(it.toInstant()) },
            entity.wishlistUpdatedAt?.let { Timestamp.from(it.toInstant()) },
            Timestamp.from(entity.syncedAt.toInstant()),
        )

        return entity
    }

    private fun updateTeamMetadata(entity: TeamMetadataEntity) {
        val sql =
            """
            UPDATE team_metadata SET
                guild_id = ?, guild_name = ?, name = ?, region = ?, realm = ?, url = ?,
                last_refreshed_blizzard = ?, last_refreshed_percentiles = ?, last_refreshed_mythic_plus = ?,
                wishlist_updated_at = ?, synced_at = ?
            WHERE team_id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.guildId,
            entity.guildName,
            entity.name,
            entity.region,
            entity.realm,
            entity.url,
            entity.lastRefreshedBlizzard?.let { Timestamp.from(it.toInstant()) },
            entity.lastRefreshedPercentiles?.let { Timestamp.from(it.toInstant()) },
            entity.lastRefreshedMythicPlus?.let { Timestamp.from(it.toInstant()) },
            entity.wishlistUpdatedAt?.let { Timestamp.from(it.toInstant()) },
            Timestamp.from(entity.syncedAt.toInstant()),
            entity.teamId,
        )
    }

    private val teamMetadataRowMapper =
        RowMapper { rs, _ ->
            val guildIdValue = rs.getLong("guild_id")
            val guildId = if (rs.wasNull()) null else guildIdValue

            TeamMetadataEntity(
                teamId = rs.getLong("team_id"),
                guildId = guildId,
                guildName = rs.getString("guild_name"),
                name = rs.getString("name"),
                region = rs.getString("region"),
                realm = rs.getString("realm"),
                url = rs.getString("url"),
                lastRefreshedBlizzard = rs.getTimestamp("last_refreshed_blizzard")?.toInstant()?.atOffset(ZoneOffset.UTC),
                lastRefreshedPercentiles = rs.getTimestamp("last_refreshed_percentiles")?.toInstant()?.atOffset(ZoneOffset.UTC),
                lastRefreshedMythicPlus = rs.getTimestamp("last_refreshed_mythic_plus")?.toInstant()?.atOffset(ZoneOffset.UTC),
                wishlistUpdatedAt = rs.getTimestamp("wishlist_updated_at")?.toInstant()?.atOffset(ZoneOffset.UTC),
                syncedAt = rs.getTimestamp("synced_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
            )
        }
}
