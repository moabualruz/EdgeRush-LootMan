package com.edgerush.lootman.infrastructure.activity

import com.edgerush.datasync.entity.HistoricalActivityEntity
import com.edgerush.lootman.domain.activity.repository.HistoricalActivityRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JdbcHistoricalActivityRepository(private val jdbcTemplate: JdbcTemplate) : HistoricalActivityRepository {
    override fun findById(id: Long): HistoricalActivityEntity? =
        jdbcTemplate.query("SELECT * FROM historical_activity WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM historical_activity WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<HistoricalActivityEntity> =
        jdbcTemplate.query("SELECT * FROM historical_activity ORDER BY synced_at DESC LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM historical_activity", Long::class.java) ?: 0L

    override fun findByCharacterId(
        characterId: Long,
        offset: Long,
        limit: Int,
    ): List<HistoricalActivityEntity> =
        jdbcTemplate.query(
            "SELECT * FROM historical_activity WHERE character_id = ? ORDER BY synced_at DESC LIMIT ? OFFSET ?",
            rowMapper,
            characterId,
            limit,
            offset,
        )

    override fun countByCharacterId(characterId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM historical_activity WHERE character_id = ?", Long::class.java, characterId) ?: 0L

    override fun findByTeamId(
        teamId: Long,
        offset: Long,
        limit: Int,
    ): List<HistoricalActivityEntity> =
        jdbcTemplate.query(
            "SELECT * FROM historical_activity WHERE team_id = ? ORDER BY synced_at DESC LIMIT ? OFFSET ?",
            rowMapper,
            teamId,
            limit,
            offset,
        )

    override fun countByTeamId(teamId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM historical_activity WHERE team_id = ?", Long::class.java, teamId) ?: 0L

    override fun save(entity: HistoricalActivityEntity): HistoricalActivityEntity =
        if (entity.id == null) {
            insert(entity)
        } else {
            update(entity)
            entity
        }

    override fun delete(id: Long) {
        jdbcTemplate.update("DELETE FROM historical_activity WHERE id = ?", id)
    }

    private fun insert(entity: HistoricalActivityEntity): HistoricalActivityEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps =
                conn.prepareStatement(
                    "INSERT INTO historical_activity (character_id, character_name, character_realm, period_id, team_id, season_id, data_json, synced_at) VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS,
                )
            entity.characterId?.let { ps.setLong(1, it) } ?: ps.setNull(1, java.sql.Types.BIGINT)
            ps.setString(2, entity.characterName)
            entity.characterRealm?.let { ps.setString(3, it) } ?: ps.setNull(3, java.sql.Types.VARCHAR)
            entity.periodId?.let { ps.setLong(4, it) } ?: ps.setNull(4, java.sql.Types.BIGINT)
            entity.teamId?.let { ps.setLong(5, it) } ?: ps.setNull(5, java.sql.Types.BIGINT)
            entity.seasonId?.let { ps.setLong(6, it) } ?: ps.setNull(6, java.sql.Types.BIGINT)
            ps.setString(7, entity.dataJson)
            ps.setTimestamp(8, Timestamp.from(entity.syncedAt.toInstant()))
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: HistoricalActivityEntity) {
        jdbcTemplate.update(
            "UPDATE historical_activity SET character_id=?, character_name=?, character_realm=?, period_id=?, team_id=?, season_id=?, data_json=?, synced_at=? WHERE id=?",
            entity.characterId, entity.characterName, entity.characterRealm, entity.periodId, entity.teamId, entity.seasonId, entity.dataJson,
            Timestamp.from(
                entity.syncedAt.toInstant(),
            ),
            entity.id,
        )
    }

    private val rowMapper =
        RowMapper { rs, _ ->
            fun getLongOrNull(col: String): Long? {
                val v = rs.getLong(col)
                return if (rs.wasNull()) null else v
            }
            HistoricalActivityEntity(
                rs.getLong("id"), getLongOrNull("character_id"), rs.getString("character_name"),
                rs.getString("character_realm"), getLongOrNull("period_id"), getLongOrNull("team_id"),
                getLongOrNull("season_id"), rs.getString("data_json"),
                rs.getTimestamp("synced_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
            )
        }
}
