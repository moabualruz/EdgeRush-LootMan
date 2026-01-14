package com.edgerush.lootman.infrastructure.gear

import com.edgerush.datasync.entity.RaiderGearItemEntity
import com.edgerush.lootman.domain.gear.repository.RaiderGearItemRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcRaiderGearItemRepository(private val jdbcTemplate: JdbcTemplate) : RaiderGearItemRepository {

    override fun findById(id: Long): RaiderGearItemEntity? =
        jdbcTemplate.query("SELECT * FROM raider_gear_items WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_gear_items WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<RaiderGearItemEntity> =
        jdbcTemplate.query("SELECT * FROM raider_gear_items ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_gear_items", Long::class.java) ?: 0L

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderGearItemEntity> =
        jdbcTemplate.query("SELECT * FROM raider_gear_items WHERE raider_id = ? ORDER BY slot LIMIT ? OFFSET ?", rowMapper, raiderId, limit, offset)

    override fun countByRaiderId(raiderId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_gear_items WHERE raider_id = ?", Long::class.java, raiderId) ?: 0L

    override fun findByRaiderIdAndGearSet(raiderId: Long, gearSet: String, offset: Long, limit: Int): List<RaiderGearItemEntity> =
        jdbcTemplate.query("SELECT * FROM raider_gear_items WHERE raider_id = ? AND gear_set = ? ORDER BY slot LIMIT ? OFFSET ?", rowMapper, raiderId, gearSet, limit, offset)

    override fun countByRaiderIdAndGearSet(raiderId: Long, gearSet: String): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_gear_items WHERE raider_id = ? AND gear_set = ?", Long::class.java, raiderId, gearSet) ?: 0L

    override fun save(entity: RaiderGearItemEntity): RaiderGearItemEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM raider_gear_items WHERE id = ?", id) }

    private fun insert(entity: RaiderGearItemEntity): RaiderGearItemEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                "INSERT INTO raider_gear_items (raider_id, gear_set, slot, item_id, item_level, quality, enchant, enchant_quality, upgrade_level, sockets, name) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, entity.raiderId)
            ps.setString(2, entity.gearSet)
            ps.setString(3, entity.slot)
            entity.itemId?.let { ps.setLong(4, it) } ?: ps.setNull(4, java.sql.Types.BIGINT)
            entity.itemLevel?.let { ps.setInt(5, it) } ?: ps.setNull(5, java.sql.Types.INTEGER)
            entity.quality?.let { ps.setInt(6, it) } ?: ps.setNull(6, java.sql.Types.INTEGER)
            entity.enchant?.let { ps.setString(7, it) } ?: ps.setNull(7, java.sql.Types.VARCHAR)
            entity.enchantQuality?.let { ps.setInt(8, it) } ?: ps.setNull(8, java.sql.Types.INTEGER)
            entity.upgradeLevel?.let { ps.setInt(9, it) } ?: ps.setNull(9, java.sql.Types.INTEGER)
            entity.sockets?.let { ps.setInt(10, it) } ?: ps.setNull(10, java.sql.Types.INTEGER)
            entity.name?.let { ps.setString(11, it) } ?: ps.setNull(11, java.sql.Types.VARCHAR)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: RaiderGearItemEntity) {
        jdbcTemplate.update(
            "UPDATE raider_gear_items SET raider_id=?, gear_set=?, slot=?, item_id=?, item_level=?, quality=?, enchant=?, enchant_quality=?, upgrade_level=?, sockets=?, name=? WHERE id=?",
            entity.raiderId, entity.gearSet, entity.slot, entity.itemId, entity.itemLevel, entity.quality,
            entity.enchant, entity.enchantQuality, entity.upgradeLevel, entity.sockets, entity.name, entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getLongOrNull(col: String): Long? { val v = rs.getLong(col); return if (rs.wasNull()) null else v }
        fun getIntOrNull(col: String): Int? { val v = rs.getInt(col); return if (rs.wasNull()) null else v }
        RaiderGearItemEntity(
            rs.getLong("id"), rs.getLong("raider_id"), rs.getString("gear_set"), rs.getString("slot"),
            getLongOrNull("item_id"), getIntOrNull("item_level"), getIntOrNull("quality"),
            rs.getString("enchant"), getIntOrNull("enchant_quality"), getIntOrNull("upgrade_level"),
            getIntOrNull("sockets"), rs.getString("name")
        )
    }
}
