package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardOldItemEntity
import com.edgerush.lootman.domain.loot.repository.LootAwardOldItemRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcLootAwardOldItemRepository(private val jdbcTemplate: JdbcTemplate) : LootAwardOldItemRepository {
    override fun findById(id: Long): LootAwardOldItemEntity? =
        jdbcTemplate.query("SELECT * FROM loot_award_old_items WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loot_award_old_items WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<LootAwardOldItemEntity> =
        jdbcTemplate.query("SELECT * FROM loot_award_old_items ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loot_award_old_items", Long::class.java) ?: 0L

    override fun findByLootAwardId(
        lootAwardId: Long,
        offset: Long,
        limit: Int,
    ): List<LootAwardOldItemEntity> =
        jdbcTemplate.query(
            "SELECT * FROM loot_award_old_items WHERE loot_award_id = ? ORDER BY id LIMIT ? OFFSET ?",
            rowMapper,
            lootAwardId,
            limit,
            offset,
        )

    override fun countByLootAwardId(lootAwardId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loot_award_old_items WHERE loot_award_id = ?", Long::class.java, lootAwardId) ?: 0L

    override fun save(entity: LootAwardOldItemEntity): LootAwardOldItemEntity =
        if (entity.id == null) {
            insert(entity)
        } else {
            update(entity)
            entity
        }

    override fun delete(id: Long) {
        jdbcTemplate.update("DELETE FROM loot_award_old_items WHERE id = ?", id)
    }

    private fun insert(entity: LootAwardOldItemEntity): LootAwardOldItemEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps =
                conn.prepareStatement(
                    "INSERT INTO loot_award_old_items (loot_award_id, item_id, bonus_id) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS,
                )
            ps.setLong(1, entity.lootAwardId)
            entity.itemId?.let { ps.setLong(2, it) } ?: ps.setNull(2, java.sql.Types.BIGINT)
            entity.bonusId?.let { ps.setString(3, it) } ?: ps.setNull(3, java.sql.Types.VARCHAR)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: LootAwardOldItemEntity) {
        jdbcTemplate.update(
            "UPDATE loot_award_old_items SET loot_award_id=?, item_id=?, bonus_id=? WHERE id=?",
            entity.lootAwardId,
            entity.itemId,
            entity.bonusId,
            entity.id,
        )
    }

    private val rowMapper =
        RowMapper { rs, _ ->
            fun getLongOrNull(col: String): Long? {
                val v = rs.getLong(col)
                return if (rs.wasNull()) null else v
            }
            LootAwardOldItemEntity(rs.getLong("id"), rs.getLong("loot_award_id"), getLongOrNull("item_id"), rs.getString("bonus_id"))
        }
}
