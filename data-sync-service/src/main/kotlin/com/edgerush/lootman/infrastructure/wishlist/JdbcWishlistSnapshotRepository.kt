package com.edgerush.lootman.infrastructure.wishlist

import com.edgerush.datasync.entity.WishlistSnapshotEntity
import com.edgerush.lootman.domain.wishlist.repository.WishlistSnapshotRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JdbcWishlistSnapshotRepository(private val jdbcTemplate: JdbcTemplate) : WishlistSnapshotRepository {

    override fun findById(id: Long): WishlistSnapshotEntity? =
        jdbcTemplate.query("SELECT * FROM wishlist_snapshots WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wishlist_snapshots WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<WishlistSnapshotEntity> =
        jdbcTemplate.query("SELECT * FROM wishlist_snapshots ORDER BY synced_at DESC LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wishlist_snapshots", Long::class.java) ?: 0L

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<WishlistSnapshotEntity> =
        jdbcTemplate.query("SELECT * FROM wishlist_snapshots WHERE raider_id = ? ORDER BY synced_at DESC LIMIT ? OFFSET ?", rowMapper, raiderId, limit, offset)

    override fun countByRaiderId(raiderId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wishlist_snapshots WHERE raider_id = ?", Long::class.java, raiderId) ?: 0L

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<WishlistSnapshotEntity> =
        jdbcTemplate.query("SELECT * FROM wishlist_snapshots WHERE team_id = ? ORDER BY synced_at DESC LIMIT ? OFFSET ?", rowMapper, teamId, limit, offset)

    override fun countByTeamId(teamId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wishlist_snapshots WHERE team_id = ?", Long::class.java, teamId) ?: 0L

    override fun save(entity: WishlistSnapshotEntity): WishlistSnapshotEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM wishlist_snapshots WHERE id = ?", id) }

    private fun insert(entity: WishlistSnapshotEntity): WishlistSnapshotEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                "INSERT INTO wishlist_snapshots (raider_id, character_name, character_realm, character_region, team_id, season_id, period_id, raw_payload, synced_at) VALUES (?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
            entity.raiderId?.let { ps.setLong(1, it) } ?: ps.setNull(1, java.sql.Types.BIGINT)
            ps.setString(2, entity.characterName)
            ps.setString(3, entity.characterRealm)
            entity.characterRegion?.let { ps.setString(4, it) } ?: ps.setNull(4, java.sql.Types.VARCHAR)
            entity.teamId?.let { ps.setLong(5, it) } ?: ps.setNull(5, java.sql.Types.BIGINT)
            entity.seasonId?.let { ps.setLong(6, it) } ?: ps.setNull(6, java.sql.Types.BIGINT)
            entity.periodId?.let { ps.setLong(7, it) } ?: ps.setNull(7, java.sql.Types.BIGINT)
            ps.setString(8, entity.rawPayload)
            ps.setTimestamp(9, Timestamp.from(entity.syncedAt.toInstant()))
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: WishlistSnapshotEntity) {
        jdbcTemplate.update(
            "UPDATE wishlist_snapshots SET raider_id=?, character_name=?, character_realm=?, character_region=?, team_id=?, season_id=?, period_id=?, raw_payload=?, synced_at=? WHERE id=?",
            entity.raiderId, entity.characterName, entity.characterRealm, entity.characterRegion,
            entity.teamId, entity.seasonId, entity.periodId, entity.rawPayload, Timestamp.from(entity.syncedAt.toInstant()), entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getLongOrNull(col: String): Long? { val v = rs.getLong(col); return if (rs.wasNull()) null else v }
        WishlistSnapshotEntity(
            rs.getLong("id"), getLongOrNull("raider_id"), rs.getString("character_name"),
            rs.getString("character_realm"), rs.getString("character_region"),
            getLongOrNull("team_id"), getLongOrNull("season_id"), getLongOrNull("period_id"),
            rs.getString("raw_payload"),
            rs.getTimestamp("synced_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now()
        )
    }
}
