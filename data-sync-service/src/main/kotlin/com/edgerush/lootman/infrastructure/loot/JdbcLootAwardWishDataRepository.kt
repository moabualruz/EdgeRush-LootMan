package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardWishDataEntity
import com.edgerush.lootman.domain.loot.repository.LootAwardWishDataRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcLootAwardWishDataRepository(private val jdbcTemplate: JdbcTemplate) : LootAwardWishDataRepository {

    override fun findById(id: Long): LootAwardWishDataEntity? =
        jdbcTemplate.query("SELECT * FROM loot_award_wish_data WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loot_award_wish_data WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<LootAwardWishDataEntity> =
        jdbcTemplate.query("SELECT * FROM loot_award_wish_data ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loot_award_wish_data", Long::class.java) ?: 0L

    override fun findByLootAwardId(lootAwardId: Long, offset: Long, limit: Int): List<LootAwardWishDataEntity> =
        jdbcTemplate.query("SELECT * FROM loot_award_wish_data WHERE loot_award_id = ? ORDER BY id LIMIT ? OFFSET ?", rowMapper, lootAwardId, limit, offset)

    override fun countByLootAwardId(lootAwardId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loot_award_wish_data WHERE loot_award_id = ?", Long::class.java, lootAwardId) ?: 0L

    override fun save(entity: LootAwardWishDataEntity): LootAwardWishDataEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM loot_award_wish_data WHERE id = ?", id) }

    private fun insert(entity: LootAwardWishDataEntity): LootAwardWishDataEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                "INSERT INTO loot_award_wish_data (loot_award_id, spec_name, spec_icon, value) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, entity.lootAwardId)
            entity.specName?.let { ps.setString(2, it) } ?: ps.setNull(2, java.sql.Types.VARCHAR)
            entity.specIcon?.let { ps.setString(3, it) } ?: ps.setNull(3, java.sql.Types.VARCHAR)
            entity.value?.let { ps.setInt(4, it) } ?: ps.setNull(4, java.sql.Types.INTEGER)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: LootAwardWishDataEntity) {
        jdbcTemplate.update(
            "UPDATE loot_award_wish_data SET loot_award_id=?, spec_name=?, spec_icon=?, value=? WHERE id=?",
            entity.lootAwardId, entity.specName, entity.specIcon, entity.value, entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getIntOrNull(col: String): Int? { val v = rs.getInt(col); return if (rs.wasNull()) null else v }
        LootAwardWishDataEntity(rs.getLong("id"), rs.getLong("loot_award_id"), rs.getString("spec_name"), rs.getString("spec_icon"), getIntOrNull("value"))
    }
}
