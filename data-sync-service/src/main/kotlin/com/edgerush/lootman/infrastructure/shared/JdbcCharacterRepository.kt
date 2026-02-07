package com.edgerush.lootman.infrastructure.shared

import com.edgerush.lootman.domain.shared.AccountId
import com.edgerush.lootman.domain.shared.CharacterId
import com.edgerush.lootman.domain.shared.model.BaseCharacter
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.WoWCharacter
import com.edgerush.lootman.domain.shared.repository.CharacterRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.time.Instant

/**
 * JDBC implementation of CharacterRepository.
 *
 * Persists character identity data to the characters table.
 * Characters are the authoritative source for WoW character identity,
 * independent of guild membership.
 */
@Repository
class JdbcCharacterRepository(
    private val jdbcTemplate: JdbcTemplate,
) : CharacterRepository {
    override fun findById(id: CharacterId): WoWCharacter? {
        val sql =
            """
            SELECT id, name, realm, region, character_class, blizzard_id, account_id,
                   created_at, updated_at
            FROM characters
            WHERE id = ?
            """.trimIndent()

        return jdbcTemplate.query(sql, characterRowMapper, id.value).firstOrNull()
    }

    override fun findByBlizzardId(blizzardId: Long): WoWCharacter? {
        val sql =
            """
            SELECT id, name, realm, region, character_class, blizzard_id, account_id,
                   created_at, updated_at
            FROM characters
            WHERE blizzard_id = ?
            """.trimIndent()

        return jdbcTemplate.query(sql, characterRowMapper, blizzardId).firstOrNull()
    }

    override fun findByNameRealmRegion(
        name: String,
        realm: String,
        region: String,
    ): WoWCharacter? {
        val sql =
            """
            SELECT id, name, realm, region, character_class, blizzard_id, account_id,
                   created_at, updated_at
            FROM characters
            WHERE LOWER(name) = LOWER(?)
              AND LOWER(realm) = LOWER(?)
              AND LOWER(region) = LOWER(?)
            """.trimIndent()

        return jdbcTemplate.query(sql, characterRowMapper, name, realm, region).firstOrNull()
    }

    override fun findByAccountId(accountId: AccountId): List<WoWCharacter> {
        val sql =
            """
            SELECT id, name, realm, region, character_class, blizzard_id, account_id,
                   created_at, updated_at
            FROM characters
            WHERE account_id = ?
            ORDER BY name
            """.trimIndent()

        return jdbcTemplate.query(sql, characterRowMapper, accountId.value)
    }

    override fun existsByNameRealmRegion(
        name: String,
        realm: String,
        region: String,
    ): Boolean {
        val sql =
            """
            SELECT COUNT(*) FROM characters
            WHERE LOWER(name) = LOWER(?)
              AND LOWER(realm) = LOWER(?)
              AND LOWER(region) = LOWER(?)
            """.trimIndent()

        val count = jdbcTemplate.queryForObject(sql, Long::class.java, name, realm, region) ?: 0L
        return count > 0
    }

    override fun getOrCreateCharacterId(
        name: String,
        realm: String,
        region: String,
        characterClass: CharacterClass,
    ): CharacterId {
        // Try to find existing character first
        val existing = findByNameRealmRegion(name, realm, region)
        if (existing != null) {
            return existing.characterId
        }

        // Create new character
        val sql =
            """
            INSERT INTO characters (name, realm, region, character_class, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (name, realm, region) DO UPDATE SET updated_at = EXCLUDED.updated_at
            RETURNING id
            """.trimIndent()

        val now = Instant.now()
        val id =
            jdbcTemplate.queryForObject(
                sql,
                Long::class.java,
                name,
                realm,
                region,
                characterClass.name,
                java.sql.Timestamp.from(now),
                java.sql.Timestamp.from(now),
            )

        return CharacterId(id!!)
    }

    override fun save(character: WoWCharacter): WoWCharacter {
        val existingId =
            if (character.characterId.value > 0) {
                findById(character.characterId)?.characterId?.value
            } else {
                null
            }

        return if (existingId != null) {
            update(character)
        } else {
            insert(character)
        }
    }

    private fun insert(character: WoWCharacter): WoWCharacter {
        val sql =
            """
            INSERT INTO characters (name, realm, region, character_class, blizzard_id, account_id,
                                   created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        val keyHolder = GeneratedKeyHolder()

        val blizzardIdValue = character.blizzardId
        val accountIdValue = character.accountId

        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, character.name)
            ps.setString(2, character.realm)
            ps.setString(3, character.region)
            ps.setString(4, character.characterClass.name)
            if (blizzardIdValue != null) {
                ps.setLong(5, blizzardIdValue)
            } else {
                ps.setNull(5, java.sql.Types.BIGINT)
            }
            if (accountIdValue != null) {
                ps.setLong(6, accountIdValue.value)
            } else {
                ps.setNull(6, java.sql.Types.BIGINT)
            }
            ps.setTimestamp(7, java.sql.Timestamp.from(character.createdAt))
            ps.setTimestamp(8, java.sql.Timestamp.from(character.updatedAt))
            ps
        }, keyHolder)

        val newId =
            (keyHolder.keys?.get("id") as? Number)?.toLong()
                ?: throw IllegalStateException("Failed to get generated ID")

        return BaseCharacter(
            characterId = CharacterId(newId),
            name = character.name,
            realm = character.realm,
            region = character.region,
            characterClass = character.characterClass,
            blizzardId = character.blizzardId,
            accountId = character.accountId,
            createdAt = character.createdAt,
            updatedAt = character.updatedAt,
        )
    }

    private fun update(character: WoWCharacter): WoWCharacter {
        val sql =
            """
            UPDATE characters
            SET name = ?, realm = ?, region = ?, character_class = ?,
                blizzard_id = ?, account_id = ?, updated_at = ?
            WHERE id = ?
            """.trimIndent()

        val now = Instant.now()
        jdbcTemplate.update(
            sql,
            character.name,
            character.realm,
            character.region,
            character.characterClass.name,
            character.blizzardId,
            character.accountId?.value,
            java.sql.Timestamp.from(now),
            character.characterId.value,
        )

        return BaseCharacter(
            characterId = character.characterId,
            name = character.name,
            realm = character.realm,
            region = character.region,
            characterClass = character.characterClass,
            blizzardId = character.blizzardId,
            accountId = character.accountId,
            createdAt = character.createdAt,
            updatedAt = now,
        )
    }

    override fun linkToAccount(
        characterId: CharacterId,
        accountId: AccountId,
    ) {
        val sql =
            """
            UPDATE characters SET account_id = ?, updated_at = ? WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            accountId.value,
            java.sql.Timestamp.from(Instant.now()),
            characterId.value,
        )
    }

    override fun linkToBlizzard(
        characterId: CharacterId,
        blizzardId: Long,
    ) {
        val sql =
            """
            UPDATE characters SET blizzard_id = ?, updated_at = ? WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            blizzardId,
            java.sql.Timestamp.from(Instant.now()),
            characterId.value,
        )
    }

    private val characterRowMapper =
        RowMapper { rs, _ ->
            val accountIdValue = rs.getLong("account_id")
            val accountId = if (rs.wasNull() || accountIdValue <= 0) null else AccountId(accountIdValue)

            val blizzardIdValue = rs.getLong("blizzard_id")
            val blizzardId = if (rs.wasNull() || blizzardIdValue <= 0) null else blizzardIdValue

            val createdAtTimestamp = rs.getTimestamp("created_at")
            val createdAt = createdAtTimestamp?.toInstant() ?: Instant.now()

            val updatedAtTimestamp = rs.getTimestamp("updated_at")
            val updatedAt = updatedAtTimestamp?.toInstant() ?: Instant.now()

            val characterClassStr = rs.getString("character_class") ?: "UNKNOWN"
            val characterClass = CharacterClass.fromString(characterClassStr)

            BaseCharacter(
                characterId = CharacterId(rs.getLong("id")),
                name = rs.getString("name"),
                realm = rs.getString("realm"),
                region = rs.getString("region") ?: "eu",
                characterClass = characterClass,
                blizzardId = blizzardId,
                accountId = accountId,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
}
