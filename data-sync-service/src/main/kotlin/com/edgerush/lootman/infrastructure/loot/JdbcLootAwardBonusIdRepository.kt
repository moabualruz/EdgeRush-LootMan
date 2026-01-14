package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardBonusIdEntity
import com.edgerush.lootman.domain.loot.repository.LootAwardBonusIdRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcLootAwardBonusIdRepository(private val jdbcTemplate: JdbcTemplate) : LootAwardBonusIdRepository {

    override fun findById(id: Long): LootAwardBonusIdEntity? =
        jdbcTemplate.query("SELECT * FROM loot_award_bonus_ids WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loot_award_bonus_ids WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<LootAwardBonusIdEntity> =
        jdbcTemplate.query("SELECT * FROM loot_award_bonus_ids ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loot_award_bonus_ids", Long::class.java) ?: 0L

    override fun findByLootAwardId(lootAwardId: Long, offset: Long, limit: Int): List<LootAwardBonusIdEntity> =
        jdbcTemplate.query("SELECT * FROM loot_award_bonus_ids WHERE loot_award_id = ? ORDER BY id LIMIT ? OFFSET ?", rowMapper, lootAwardId, limit, offset)

    override fun countByLootAwardId(lootAwardId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loot_award_bonus_ids WHERE loot_award_id = ?", Long::class.java, lootAwardId) ?: 0L

    override fun save(entity: LootAwardBonusIdEntity): LootAwardBonusIdEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM loot_award_bonus_ids WHERE id = ?", id) }

    private fun insert(entity: LootAwardBonusIdEntity): LootAwardBonusIdEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                "INSERT INTO loot_award_bonus_ids (loot_award_id, bonus_id) VALUES (?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, entity.lootAwardId)
            entity.bonusId?.let { ps.setString(2, it) } ?: ps.setNull(2, java.sql.Types.VARCHAR)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: LootAwardBonusIdEntity) {
        jdbcTemplate.update(
            "UPDATE loot_award_bonus_ids SET loot_award_id=?, bonus_id=? WHERE id=?",
            entity.lootAwardId, entity.bonusId, entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        LootAwardBonusIdEntity(rs.getLong("id"), rs.getLong("loot_award_id"), rs.getString("bonus_id"))
    }
}
