package com.edgerush.lootman.infrastructure.auth

import com.edgerush.lootman.domain.auth.model.UserCharacter
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp

@Repository
class JdbcUserCharacterRepository(
    private val jdbcTemplate: JdbcTemplate
) : UserCharacterRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _ ->
        UserCharacter(
            id = rs.getLong("id"),
            userId = UserId(rs.getLong("user_id")),
            name = rs.getString("character_name"),
            realm = rs.getString("realm"),
            className = rs.getString("class_name") ?: "Unknown",
            classId = rs.getInt("class_id").takeUnless { rs.wasNull() },
            specId = rs.getInt("spec_id").takeUnless { rs.wasNull() },
            level = rs.getInt("level"),
            race = rs.getString("playable_race"),
            faction = rs.getString("faction"),
            blizzardId = rs.getLong("blizzard_id").takeUnless { rs.wasNull() },
            lastSyncedAt = rs.getTimestamp("last_synced_at").toInstant()
        )
    }

    override fun save(character: UserCharacter): UserCharacter {
        return if (character.id == null) {
            insert(character)
        } else {
            update(character)
        }
    }

    override fun saveAll(characters: List<UserCharacter>): List<UserCharacter> {
        if (characters.isEmpty()) return emptyList()
        deleteAllByUserId(characters.first().userId) // Simple sync strategy: wipe and replace
        return characters.map { insert(it) }
    }

    private fun insert(character: UserCharacter): UserCharacter {
        val sql = """
            INSERT INTO user_characters (
                user_id, character_name, realm, class_name, class_id, spec_id, level,
                playable_race, faction, blizzard_id, last_synced_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """
        val id = jdbcTemplate.queryForObject(
            sql,
            Long::class.java,
            character.userId.value,
            character.name,
            character.realm,
            character.className,
            character.classId,
            character.specId,
            character.level,
            character.race,
            character.faction,
            character.blizzardId,
            Timestamp.from(character.lastSyncedAt)
        )
        return character.copy(id = id)
    }

    private fun update(character: UserCharacter): UserCharacter {
        val sql = """
            UPDATE user_characters SET
                character_name = ?, realm = ?, class_name = ?, class_id = ?, spec_id = ?, level = ?,
                playable_race = ?, faction = ?, blizzard_id = ?, last_synced_at = ?
            WHERE id = ?
        """
        jdbcTemplate.update(
            sql,
            character.name,
            character.realm,
            character.className,
            character.classId,
            character.specId,
            character.level,
            character.race,
            character.faction,
            character.blizzardId,
            Timestamp.from(character.lastSyncedAt),
            character.id
        )
        return character
    }

    override fun findAllByUserId(userId: UserId): List<UserCharacter> {
        val sql = "SELECT * FROM user_characters WHERE user_id = ? ORDER BY level DESC, character_name ASC"
        return jdbcTemplate.query(sql, rowMapper, userId.value)
    }

    override fun deleteAllByUserId(userId: UserId) {
        val sql = "DELETE FROM user_characters WHERE user_id = ?"
        jdbcTemplate.update(sql, userId.value)
    }
}
