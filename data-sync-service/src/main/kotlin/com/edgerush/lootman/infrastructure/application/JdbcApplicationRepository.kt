package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationEntity
import com.edgerush.lootman.domain.application.repository.ApplicationRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JdbcApplicationRepository(private val jdbcTemplate: JdbcTemplate) : ApplicationRepository {
    override fun findById(id: Long): ApplicationEntity? =
        jdbcTemplate.query("SELECT * FROM applications WHERE application_id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications WHERE application_id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<ApplicationEntity> =
        jdbcTemplate.query("SELECT * FROM applications ORDER BY applied_at DESC LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications", Long::class.java) ?: 0L

    override fun findByStatus(
        status: String,
        offset: Long,
        limit: Int,
    ): List<ApplicationEntity> =
        jdbcTemplate.query(
            "SELECT * FROM applications WHERE status = ? ORDER BY applied_at DESC LIMIT ? OFFSET ?",
            rowMapper,
            status,
            limit,
            offset,
        )

    override fun countByStatus(status: String): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications WHERE status = ?", Long::class.java, status) ?: 0L

    override fun save(entity: ApplicationEntity): ApplicationEntity {
        val exists = existsById(entity.applicationId)
        if (exists) {
            update(entity)
        } else {
            insert(entity)
        }
        return entity
    }

    override fun delete(id: Long) {
        jdbcTemplate.update("DELETE FROM applications WHERE application_id = ?", id)
    }

    private fun insert(entity: ApplicationEntity) {
        jdbcTemplate.update(
            """INSERT INTO applications (application_id, applied_at, status, role, age, country, battletag, discord_id,
               main_character_name, main_character_realm, main_character_class, main_character_role,
               main_character_race, main_character_faction, main_character_level, main_character_region, synced_at)
               VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
            entity.applicationId,
            entity.appliedAt?.let { Timestamp.from(it.toInstant()) },
            entity.status, entity.role, entity.age, entity.country, entity.battletag, entity.discordId,
            entity.mainCharacterName, entity.mainCharacterRealm, entity.mainCharacterClass, entity.mainCharacterRole,
            entity.mainCharacterRace, entity.mainCharacterFaction, entity.mainCharacterLevel, entity.mainCharacterRegion,
            Timestamp.from(entity.syncedAt.toInstant()),
        )
    }

    private fun update(entity: ApplicationEntity) {
        jdbcTemplate.update(
            """UPDATE applications SET applied_at=?, status=?, role=?, age=?, country=?, battletag=?, discord_id=?,
               main_character_name=?, main_character_realm=?, main_character_class=?, main_character_role=?,
               main_character_race=?, main_character_faction=?, main_character_level=?, main_character_region=?, synced_at=?
               WHERE application_id=?""",
            entity.appliedAt?.let { Timestamp.from(it.toInstant()) },
            entity.status, entity.role, entity.age, entity.country, entity.battletag, entity.discordId,
            entity.mainCharacterName, entity.mainCharacterRealm, entity.mainCharacterClass, entity.mainCharacterRole,
            entity.mainCharacterRace, entity.mainCharacterFaction, entity.mainCharacterLevel, entity.mainCharacterRegion,
            Timestamp.from(entity.syncedAt.toInstant()), entity.applicationId,
        )
    }

    private val rowMapper =
        RowMapper { rs, _ ->
            fun getIntOrNull(col: String): Int? {
                val v = rs.getInt(col)
                return if (rs.wasNull()) null else v
            }

            fun getTimestampOrNull(col: String): OffsetDateTime? = rs.getTimestamp(col)?.toInstant()?.atOffset(ZoneOffset.UTC)
            ApplicationEntity(
                rs.getLong("application_id"),
                getTimestampOrNull("applied_at"),
                rs.getString("status"), rs.getString("role"), getIntOrNull("age"),
                rs.getString("country"), rs.getString("battletag"), rs.getString("discord_id"),
                rs.getString("main_character_name"), rs.getString("main_character_realm"),
                rs.getString("main_character_class"), rs.getString("main_character_role"),
                rs.getString("main_character_race"), rs.getString("main_character_faction"),
                getIntOrNull("main_character_level"), rs.getString("main_character_region"),
                getTimestampOrNull("synced_at") ?: OffsetDateTime.now(),
            )
        }
}
