package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JdbcRaiderEntityRepository(
    private val jdbcTemplate: JdbcTemplate,
) : RaiderEntityRepository {
    override fun findById(id: Long): RaiderEntity? {
        val sql =
            """
            SELECT id, character_name, realm, region, wowaudit_id, class, spec, role, rank, status,
                   note, blizzard_id, tracking_since, join_date, blizzard_last_modified, last_sync
            FROM raiders WHERE id = ?
            """.trimIndent()
        return jdbcTemplate.query(sql, raiderRowMapper, id).firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raiders WHERE id = ?", Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderEntity> {
        val sql =
            """
            SELECT id, character_name, realm, region, wowaudit_id, class, spec, role, rank, status,
                   note, blizzard_id, tracking_since, join_date, blizzard_last_modified, last_sync
            FROM raiders ORDER BY last_sync DESC, id LIMIT ? OFFSET ?
            """.trimIndent()
        return jdbcTemplate.query(sql, raiderRowMapper, limit, offset)
    }

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raiders", Long::class.java) ?: 0L

    override fun findByRealm(
        realm: String,
        offset: Long,
        limit: Int,
    ): List<RaiderEntity> {
        val sql =
            """
            SELECT id, character_name, realm, region, wowaudit_id, class, spec, role, rank, status,
                   note, blizzard_id, tracking_since, join_date, blizzard_last_modified, last_sync
            FROM raiders WHERE realm = ? ORDER BY last_sync DESC, id LIMIT ? OFFSET ?
            """.trimIndent()
        return jdbcTemplate.query(sql, raiderRowMapper, realm, limit, offset)
    }

    override fun countByRealm(realm: String): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raiders WHERE realm = ?", Long::class.java, realm) ?: 0L

    override fun findByRegion(
        region: String,
        offset: Long,
        limit: Int,
    ): List<RaiderEntity> {
        val sql =
            """
            SELECT id, character_name, realm, region, wowaudit_id, class, spec, role, rank, status,
                   note, blizzard_id, tracking_since, join_date, blizzard_last_modified, last_sync
            FROM raiders WHERE region = ? ORDER BY last_sync DESC, id LIMIT ? OFFSET ?
            """.trimIndent()
        return jdbcTemplate.query(sql, raiderRowMapper, region, limit, offset)
    }

    override fun countByRegion(region: String): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raiders WHERE region = ?", Long::class.java, region) ?: 0L

    override fun save(entity: RaiderEntity): RaiderEntity =
        if (entity.id == null) {
            insert(entity)
        } else {
            update(entity)
            entity
        }

    override fun delete(id: Long) {
        jdbcTemplate.update("DELETE FROM raiders WHERE id = ?", id)
    }

    private fun insert(entity: RaiderEntity): RaiderEntity {
        val sql =
            """
            INSERT INTO raiders (character_name, realm, region, wowaudit_id, class, spec, role, rank, status,
                note, blizzard_id, tracking_since, join_date, blizzard_last_modified, last_sync)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, entity.characterName)
            ps.setString(2, entity.realm)
            ps.setString(3, entity.region)
            entity.wowauditId?.let { ps.setLong(4, it) } ?: ps.setNull(4, java.sql.Types.BIGINT)
            ps.setString(5, entity.clazz)
            ps.setString(6, entity.spec)
            ps.setString(7, entity.role)
            entity.rank?.let { ps.setString(8, it) } ?: ps.setNull(8, java.sql.Types.VARCHAR)
            entity.status?.let { ps.setString(9, it) } ?: ps.setNull(9, java.sql.Types.VARCHAR)
            entity.note?.let { ps.setString(10, it) } ?: ps.setNull(10, java.sql.Types.VARCHAR)
            entity.blizzardId?.let { ps.setLong(11, it) } ?: ps.setNull(11, java.sql.Types.BIGINT)
            entity.trackingSince?.let { ps.setTimestamp(12, Timestamp.from(it.toInstant())) } ?: ps.setNull(12, java.sql.Types.TIMESTAMP)
            entity.joinDate?.let { ps.setTimestamp(13, Timestamp.from(it.toInstant())) } ?: ps.setNull(13, java.sql.Types.TIMESTAMP)
            entity.blizzardLastModified?.let { ps.setTimestamp(14, Timestamp.from(it.toInstant())) } ?: ps.setNull(14, java.sql.Types.TIMESTAMP)
            ps.setTimestamp(15, Timestamp.from(entity.lastSync.toInstant()))
            ps
        }, keyHolder)
        val id = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return entity.copy(id = id?.toLong())
    }

    private fun update(entity: RaiderEntity) {
        val sql =
            """
            UPDATE raiders SET character_name=?, realm=?, region=?, wowaudit_id=?, class=?, spec=?, role=?,
                rank=?, status=?, note=?, blizzard_id=?, tracking_since=?, join_date=?, blizzard_last_modified=?, last_sync=?
            WHERE id = ?
            """.trimIndent()
        jdbcTemplate.update(
            sql, entity.characterName, entity.realm, entity.region, entity.wowauditId,
            entity.clazz, entity.spec, entity.role, entity.rank, entity.status, entity.note, entity.blizzardId,
            entity.trackingSince?.let { Timestamp.from(it.toInstant()) }, entity.joinDate?.let { Timestamp.from(it.toInstant()) },
            entity.blizzardLastModified?.let { Timestamp.from(it.toInstant()) }, Timestamp.from(entity.lastSync.toInstant()), entity.id,
        )
    }

    private val raiderRowMapper =
        RowMapper { rs, _ ->
            val wowauditIdVal = rs.getLong("wowaudit_id")
            val wowauditId = if (rs.wasNull()) null else wowauditIdVal
            val blizzardIdVal = rs.getLong("blizzard_id")
            val blizzardId = if (rs.wasNull()) null else blizzardIdVal
            RaiderEntity(
                id = rs.getLong("id"), characterName = rs.getString("character_name"), realm = rs.getString("realm"),
                region = rs.getString("region"), wowauditId = wowauditId, clazz = rs.getString("class"),
                spec = rs.getString("spec"), role = rs.getString("role"), rank = rs.getString("rank"),
                status = rs.getString("status"), note = rs.getString("note"), blizzardId = blizzardId,
                trackingSince = rs.getTimestamp("tracking_since")?.toInstant()?.atOffset(ZoneOffset.UTC),
                joinDate = rs.getTimestamp("join_date")?.toInstant()?.atOffset(ZoneOffset.UTC),
                blizzardLastModified = rs.getTimestamp("blizzard_last_modified")?.toInstant()?.atOffset(ZoneOffset.UTC),
                lastSync = rs.getTimestamp("last_sync")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
            )
        }
}
