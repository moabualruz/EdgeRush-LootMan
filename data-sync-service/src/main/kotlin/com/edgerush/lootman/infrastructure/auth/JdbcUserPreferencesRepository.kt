package com.edgerush.lootman.infrastructure.auth

import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserPreferences
import com.edgerush.lootman.domain.auth.model.UserPreferencesId
import com.edgerush.lootman.domain.auth.repository.UserPreferencesRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.Instant

/**
 * JDBC implementation of UserPreferencesRepository.
 */
@Repository
class JdbcUserPreferencesRepository(
    private val jdbcTemplate: JdbcTemplate,
) : UserPreferencesRepository {
    override fun findByUserId(userId: UserId): UserPreferences? {
        val sql =
            """
            SELECT id, user_id, active_character_mapping_id, last_guild_id, updated_at
            FROM user_preferences
            WHERE user_id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, preferencesRowMapper, userId.value)
        return results.firstOrNull()
    }

    override fun findById(id: UserPreferencesId): UserPreferences? {
        val sql =
            """
            SELECT id, user_id, active_character_mapping_id, last_guild_id, updated_at
            FROM user_preferences
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, preferencesRowMapper, id.value)
        return results.firstOrNull()
    }

    override fun save(preferences: UserPreferences): UserPreferences {
        return if (preferences.id == null) {
            insert(preferences)
        } else {
            update(preferences)
            preferences
        }
    }

    override fun updateActiveCharacter(
        userId: UserId,
        mappingId: UserCharacterMappingId?,
        guildId: GuildId?,
    ): UserPreferences {
        val existing = findByUserId(userId)

        return if (existing != null) {
            // Update existing preferences
            val updated =
                existing.copy(
                    activeCharacterMappingId = mappingId,
                    lastGuildId = guildId,
                    updatedAt = Instant.now(),
                )
            update(updated)
            updated
        } else {
            // Create new preferences
            insert(
                UserPreferences(
                    userId = userId,
                    activeCharacterMappingId = mappingId,
                    lastGuildId = guildId,
                ),
            )
        }
    }

    override fun deleteByUserId(userId: UserId) {
        val sql = "DELETE FROM user_preferences WHERE user_id = ?"
        jdbcTemplate.update(sql, userId.value)
    }

    private fun insert(preferences: UserPreferences): UserPreferences {
        val sql =
            """
            INSERT INTO user_preferences (user_id, active_character_mapping_id, last_guild_id, updated_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT ON CONSTRAINT uq_user_preferences_user
            DO UPDATE SET
                active_character_mapping_id = EXCLUDED.active_character_mapping_id,
                last_guild_id = EXCLUDED.last_guild_id,
                updated_at = EXCLUDED.updated_at
            """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, preferences.userId.value)
            preferences.activeCharacterMappingId?.let { ps.setLong(2, it.value) }
                ?: ps.setNull(2, java.sql.Types.BIGINT)
            preferences.lastGuildId?.let { ps.setString(3, it.value) }
                ?: ps.setNull(3, java.sql.Types.VARCHAR)
            ps.setTimestamp(4, Timestamp.from(preferences.updatedAt))
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return if (generatedId != null) {
            preferences.copy(id = UserPreferencesId(generatedId.toLong()))
        } else {
            // Upsert happened, find and return
            findByUserId(preferences.userId) ?: preferences
        }
    }

    private fun update(preferences: UserPreferences) {
        val sql =
            """
            UPDATE user_preferences SET
                active_character_mapping_id = ?,
                last_guild_id = ?,
                updated_at = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            preferences.activeCharacterMappingId?.value,
            preferences.lastGuildId?.value,
            Timestamp.from(preferences.updatedAt),
            preferences.id?.value,
        )
    }

    private val preferencesRowMapper =
        RowMapper { rs, _ ->
            val mappingId = rs.getLong("active_character_mapping_id")
            val guildIdStr = rs.getString("last_guild_id")

            UserPreferences(
                id = UserPreferencesId(rs.getLong("id")),
                userId = UserId(rs.getLong("user_id")),
                activeCharacterMappingId = if (rs.wasNull()) null else UserCharacterMappingId(mappingId),
                lastGuildId = guildIdStr?.let { GuildId(it) },
                updatedAt = rs.getTimestamp("updated_at")?.toInstant() ?: Instant.now(),
            )
        }
}
