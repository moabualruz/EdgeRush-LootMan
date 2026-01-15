package com.edgerush.lootman.infrastructure.character

import com.edgerush.datasync.entity.CharacterHistoryEntity
import com.edgerush.lootman.domain.character.repository.CharacterHistoryRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * JDBC implementation of CharacterHistoryRepository.
 *
 * Persists CharacterHistoryEntity to the character_history table.
 */
@Repository
class JdbcCharacterHistoryRepository(
    private val jdbcTemplate: JdbcTemplate,
) : CharacterHistoryRepository {
    override fun findById(id: Long): CharacterHistoryEntity? {
        val sql =
            """
            SELECT id, character_id, character_name, character_realm, character_region,
                   team_id, season_id, period_id, history_json, best_gear_json, synced_at
            FROM character_history
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, characterHistoryRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM character_history WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<CharacterHistoryEntity> {
        val sql =
            """
            SELECT id, character_id, character_name, character_realm, character_region,
                   team_id, season_id, period_id, history_json, best_gear_json, synced_at
            FROM character_history
            ORDER BY synced_at DESC, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, characterHistoryRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM character_history"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByCharacterId(
        characterId: Long,
        offset: Long,
        limit: Int,
    ): List<CharacterHistoryEntity> {
        val sql =
            """
            SELECT id, character_id, character_name, character_realm, character_region,
                   team_id, season_id, period_id, history_json, best_gear_json, synced_at
            FROM character_history
            WHERE character_id = ?
            ORDER BY synced_at DESC, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, characterHistoryRowMapper, characterId, limit, offset)
    }

    override fun countByCharacterId(characterId: Long): Long {
        val sql = "SELECT COUNT(*) FROM character_history WHERE character_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, characterId) ?: 0L
    }

    override fun findByTeamId(
        teamId: Long,
        offset: Long,
        limit: Int,
    ): List<CharacterHistoryEntity> {
        val sql =
            """
            SELECT id, character_id, character_name, character_realm, character_region,
                   team_id, season_id, period_id, history_json, best_gear_json, synced_at
            FROM character_history
            WHERE team_id = ?
            ORDER BY synced_at DESC, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, characterHistoryRowMapper, teamId, limit, offset)
    }

    override fun countByTeamId(teamId: Long): Long {
        val sql = "SELECT COUNT(*) FROM character_history WHERE team_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, teamId) ?: 0L
    }

    override fun save(entity: CharacterHistoryEntity): CharacterHistoryEntity {
        return if (entity.id == null) {
            insertCharacterHistory(entity)
        } else {
            updateCharacterHistory(entity)
            entity
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM character_history WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertCharacterHistory(entity: CharacterHistoryEntity): CharacterHistoryEntity {
        val sql =
            """
            INSERT INTO character_history (
                character_id, character_name, character_realm, character_region,
                team_id, season_id, period_id, history_json, best_gear_json, synced_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, entity.characterId)
            ps.setString(2, entity.characterName)
            entity.characterRealm?.let { ps.setString(3, it) } ?: ps.setNull(3, java.sql.Types.VARCHAR)
            entity.characterRegion?.let { ps.setString(4, it) } ?: ps.setNull(4, java.sql.Types.VARCHAR)
            entity.teamId?.let { ps.setLong(5, it) } ?: ps.setNull(5, java.sql.Types.BIGINT)
            entity.seasonId?.let { ps.setLong(6, it) } ?: ps.setNull(6, java.sql.Types.BIGINT)
            entity.periodId?.let { ps.setLong(7, it) } ?: ps.setNull(7, java.sql.Types.BIGINT)
            ps.setString(8, entity.historyJson)
            entity.bestGearJson?.let { ps.setString(9, it) } ?: ps.setNull(9, java.sql.Types.VARCHAR)
            ps.setTimestamp(10, Timestamp.from(entity.syncedAt.toInstant()))
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return entity.copy(id = generatedId?.toLong())
    }

    private fun updateCharacterHistory(entity: CharacterHistoryEntity) {
        val sql =
            """
            UPDATE character_history SET
                character_id = ?, character_name = ?, character_realm = ?, character_region = ?,
                team_id = ?, season_id = ?, period_id = ?, history_json = ?, best_gear_json = ?, synced_at = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.characterId,
            entity.characterName,
            entity.characterRealm,
            entity.characterRegion,
            entity.teamId,
            entity.seasonId,
            entity.periodId,
            entity.historyJson,
            entity.bestGearJson,
            Timestamp.from(entity.syncedAt.toInstant()),
            entity.id,
        )
    }

    private val characterHistoryRowMapper =
        RowMapper { rs, _ ->
            val teamIdValue = rs.getLong("team_id")
            val teamId = if (rs.wasNull()) null else teamIdValue

            val seasonIdValue = rs.getLong("season_id")
            val seasonId = if (rs.wasNull()) null else seasonIdValue

            val periodIdValue = rs.getLong("period_id")
            val periodId = if (rs.wasNull()) null else periodIdValue

            CharacterHistoryEntity(
                id = rs.getLong("id"),
                characterId = rs.getLong("character_id"),
                characterName = rs.getString("character_name"),
                characterRealm = rs.getString("character_realm"),
                characterRegion = rs.getString("character_region"),
                teamId = teamId,
                seasonId = seasonId,
                periodId = periodId,
                historyJson = rs.getString("history_json"),
                bestGearJson = rs.getString("best_gear_json"),
                syncedAt = rs.getTimestamp("synced_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
            )
        }
}
