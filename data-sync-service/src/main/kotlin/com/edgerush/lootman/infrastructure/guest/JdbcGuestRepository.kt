package com.edgerush.lootman.infrastructure.guest

import com.edgerush.datasync.entity.GuestEntity
import com.edgerush.lootman.domain.guest.repository.GuestRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JdbcGuestRepository(private val jdbcTemplate: JdbcTemplate) : GuestRepository {
    override fun findById(guestId: Long): GuestEntity? {
        val sql = "SELECT guest_id, name, realm, class, role, blizzard_id, tracking_since, synced_at FROM guests WHERE guest_id = ?"
        return jdbcTemplate.query(sql, rowMapper, guestId).firstOrNull()
    }

    override fun existsById(guestId: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM guests WHERE guest_id = ?", Int::class.java, guestId) ?: 0) > 0

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<GuestEntity> {
        val sql = "SELECT guest_id, name, realm, class, role, blizzard_id, tracking_since, synced_at FROM guests ORDER BY synced_at DESC LIMIT ? OFFSET ?"
        return jdbcTemplate.query(sql, rowMapper, limit, offset)
    }

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM guests", Long::class.java) ?: 0L

    override fun save(entity: GuestEntity): GuestEntity {
        if (existsById(entity.guestId)) {
            jdbcTemplate.update(
                "UPDATE guests SET name=?, realm=?, class=?, role=?, blizzard_id=?, tracking_since=?, synced_at=? WHERE guest_id=?",
                entity.name, entity.realm, entity.clazz, entity.role, entity.blizzardId,
                entity.trackingSince?.let { Timestamp.from(it.toInstant()) }, Timestamp.from(entity.syncedAt.toInstant()), entity.guestId,
            )
        } else {
            jdbcTemplate.update(
                "INSERT INTO guests (guest_id, name, realm, class, role, blizzard_id, tracking_since, synced_at) VALUES (?,?,?,?,?,?,?,?)",
                entity.guestId, entity.name, entity.realm, entity.clazz, entity.role, entity.blizzardId,
                entity.trackingSince?.let { Timestamp.from(it.toInstant()) }, Timestamp.from(entity.syncedAt.toInstant()),
            )
        }
        return entity
    }

    override fun delete(guestId: Long) {
        jdbcTemplate.update("DELETE FROM guests WHERE guest_id = ?", guestId)
    }

    private val rowMapper =
        RowMapper { rs, _ ->
            val blizzardIdVal = rs.getLong("blizzard_id")
            val blizzardId = if (rs.wasNull()) null else blizzardIdVal
            GuestEntity(
                rs.getLong("guest_id"),
                rs.getString("name"),
                rs.getString("realm"),
                rs.getString("class"),
                rs.getString("role"),
                blizzardId,
                rs.getTimestamp("tracking_since")?.toInstant()?.atOffset(ZoneOffset.UTC),
                rs.getTimestamp("synced_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
            )
        }
}
