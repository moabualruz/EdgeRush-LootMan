package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.raids.model.GuildId
import com.edgerush.datasync.domain.raids.model.Raid
import com.edgerush.datasync.domain.raids.model.RaidId
import com.edgerush.datasync.domain.raids.repository.RaidRepository
import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.datasync.infrastructure.persistence.mapper.RaidMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * JDBC implementation of RaidRepository.
 * Handles persistence of Raid aggregates including encounters and signups.
 */
@Repository
class JdbcRaidRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val mapper: RaidMapper
) : RaidRepository {
    
    override fun findById(id: RaidId): Raid? {
        val sql = """
            SELECT raid_id, date, start_time, end_time, instance, difficulty, optional, status,
                   present_size, total_size, notes, selections_image, team_id, season_id, 
                   period_id, created_at, updated_at, synced_at
            FROM raids
            WHERE raid_id = ?
        """.trimIndent()
        
        val entities = jdbcTemplate.query(sql, raidEntityRowMapper, id.value)
        if (entities.isEmpty()) return null
        
        val entity = entities.first()
        val encounters = findEncountersByRaidId(id.value)
        val signups = findSignupsByRaidId(id.value)
        
        // TODO: Get actual guild ID from database or context
        return mapper.toDomain(entity, "unknown-guild", encounters, signups)
    }
    
    override fun findByGuildId(guildId: GuildId): List<Raid> {
        // Note: Current schema doesn't have guild_id column
        // This is a limitation that needs to be addressed
        val sql = """
            SELECT raid_id, date, start_time, end_time, instance, difficulty, optional, status,
                   present_size, total_size, notes, selections_image, team_id, season_id,
                   period_id, created_at, updated_at, synced_at
            FROM raids
            ORDER BY date DESC
        """.trimIndent()
        
        val entities = jdbcTemplate.query(sql, raidEntityRowMapper)
        return entities.map { entity ->
            val encounters = findEncountersByRaidId(entity.raidId)
            val signups = findSignupsByRaidId(entity.raidId)
            mapper.toDomain(entity, guildId.value, encounters, signups)
        }
    }
    
    override fun findByGuildIdAndDate(guildId: GuildId, date: LocalDate): List<Raid> {
        val sql = """
            SELECT raid_id, date, start_time, end_time, instance, difficulty, optional, status,
                   present_size, total_size, notes, selections_image, team_id, season_id,
                   period_id, created_at, updated_at, synced_at
            FROM raids
            WHERE date = ?
            ORDER BY start_time
        """.trimIndent()
        
        val entities = jdbcTemplate.query(sql, raidEntityRowMapper, date)
        return entities.map { entity ->
            val encounters = findEncountersByRaidId(entity.raidId)
            val signups = findSignupsByRaidId(entity.raidId)
            mapper.toDomain(entity, guildId.value, encounters, signups)
        }
    }
    
    @Transactional
    override fun save(raid: Raid): Raid {
        val entity = mapper.toEntity(raid)
        
        // Check if raid exists
        val exists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM raids WHERE raid_id = ?",
            Int::class.java,
            entity.raidId
        ) ?: 0 > 0
        
        if (exists) {
            updateRaid(entity)
        } else {
            insertRaid(entity)
        }
        
        // Delete and re-insert encounters and signups
        deleteEncountersByRaidId(entity.raidId)
        deleteSignupsByRaidId(entity.raidId)
        
        val encounterEntities = mapper.encounterMapper.toEntities(raid.getEncounters(), entity.raidId)
        val signupEntities = mapper.signupMapper.toEntities(raid.getSignups(), entity.raidId)
        
        encounterEntities.forEach { insertEncounter(it) }
        signupEntities.forEach { insertSignup(it) }
        
        return raid
    }
    
    @Transactional
    override fun delete(id: RaidId) {
        // Cascading deletes handled by database foreign keys
        jdbcTemplate.update("DELETE FROM raids WHERE raid_id = ?", id.value)
    }
    
    // Private helper methods
    
    private fun insertRaid(entity: RaidEntity) {
        val sql = """
            INSERT INTO raids (raid_id, date, start_time, end_time, instance, difficulty, optional,
                             status, present_size, total_size, notes, selections_image, team_id,
                             season_id, period_id, created_at, updated_at, synced_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        
        jdbcTemplate.update(
            sql,
            entity.raidId,
            entity.date,
            entity.startTime,
            entity.endTime,
            entity.instance,
            entity.difficulty,
            entity.optional,
            entity.status,
            entity.presentSize,
            entity.totalSize,
            entity.notes,
            entity.selectionsImage,
            entity.teamId,
            entity.seasonId,
            entity.periodId,
            entity.createdAt,
            entity.updatedAt,
            entity.syncedAt
        )
    }
    
    private fun updateRaid(entity: RaidEntity) {
        val sql = """
            UPDATE raids
            SET date = ?, start_time = ?, end_time = ?, instance = ?, difficulty = ?,
                optional = ?, status = ?, present_size = ?, total_size = ?, notes = ?,
                selections_image = ?, team_id = ?, season_id = ?, period_id = ?,
                updated_at = ?, synced_at = ?
            WHERE raid_id = ?
        """.trimIndent()
        
        jdbcTemplate.update(
            sql,
            entity.date,
            entity.startTime,
            entity.endTime,
            entity.instance,
            entity.difficulty,
            entity.optional,
            entity.status,
            entity.presentSize,
            entity.totalSize,
            entity.notes,
            entity.selectionsImage,
            entity.teamId,
            entity.seasonId,
            entity.periodId,
            entity.updatedAt,
            entity.syncedAt,
            entity.raidId
        )
    }
    
    private fun findEncountersByRaidId(raidId: Long): List<RaidEncounterEntity> {
        val sql = """
            SELECT id, raid_id, encounter_id, name, enabled, extra, notes
            FROM raid_encounters
            WHERE raid_id = ?
        """.trimIndent()
        
        return jdbcTemplate.query(sql, encounterEntityRowMapper, raidId)
    }
    
    private fun findSignupsByRaidId(raidId: Long): List<RaidSignupEntity> {
        val sql = """
            SELECT id, raid_id, character_id, character_name, character_realm, character_region,
                   character_class, character_role, character_guest, status, comment, selected
            FROM raid_signups
            WHERE raid_id = ?
        """.trimIndent()
        
        return jdbcTemplate.query(sql, signupEntityRowMapper, raidId)
    }
    
    private fun insertEncounter(entity: RaidEncounterEntity) {
        val sql = """
            INSERT INTO raid_encounters (raid_id, encounter_id, name, enabled, extra, notes)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()
        
        jdbcTemplate.update(
            sql,
            entity.raidId,
            entity.encounterId,
            entity.name,
            entity.enabled,
            entity.extra,
            entity.notes
        )
    }
    
    private fun insertSignup(entity: RaidSignupEntity) {
        val sql = """
            INSERT INTO raid_signups (raid_id, character_id, character_name, character_realm,
                                    character_region, character_class, character_role,
                                    character_guest, status, comment, selected)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        
        jdbcTemplate.update(
            sql,
            entity.raidId,
            entity.characterId,
            entity.characterName,
            entity.characterRealm,
            entity.characterRegion,
            entity.characterClass,
            entity.characterRole,
            entity.characterGuest,
            entity.status,
            entity.comment,
            entity.selected
        )
    }
    
    private fun deleteEncountersByRaidId(raidId: Long) {
        jdbcTemplate.update("DELETE FROM raid_encounters WHERE raid_id = ?", raidId)
    }
    
    private fun deleteSignupsByRaidId(raidId: Long) {
        jdbcTemplate.update("DELETE FROM raid_signups WHERE raid_id = ?", raidId)
    }
    
    // Row mappers
    
    private val raidEntityRowMapper = RowMapper { rs, _ ->
        RaidEntity(
            raidId = rs.getLong("raid_id"),
            date = rs.getDate("date")?.toLocalDate(),
            startTime = rs.getTime("start_time")?.toLocalTime(),
            endTime = rs.getTime("end_time")?.toLocalTime(),
            instance = rs.getString("instance"),
            difficulty = rs.getString("difficulty"),
            optional = rs.getBoolean("optional"),
            status = rs.getString("status"),
            presentSize = rs.getObject("present_size") as? Int,
            totalSize = rs.getObject("total_size") as? Int,
            notes = rs.getString("notes"),
            selectionsImage = rs.getString("selections_image"),
            teamId = rs.getObject("team_id") as? Long,
            seasonId = rs.getObject("season_id") as? Long,
            periodId = rs.getObject("period_id") as? Long,
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java),
            syncedAt = rs.getObject("synced_at", OffsetDateTime::class.java)
        )
    }
    
    private val encounterEntityRowMapper = RowMapper { rs, _ ->
        RaidEncounterEntity(
            id = rs.getLong("id"),
            raidId = rs.getLong("raid_id"),
            encounterId = rs.getObject("encounter_id") as? Long,
            name = rs.getString("name"),
            enabled = rs.getBoolean("enabled"),
            extra = rs.getBoolean("extra"),
            notes = rs.getString("notes")
        )
    }
    
    private val signupEntityRowMapper = RowMapper { rs, _ ->
        RaidSignupEntity(
            id = rs.getLong("id"),
            raidId = rs.getLong("raid_id"),
            characterId = rs.getObject("character_id") as? Long,
            characterName = rs.getString("character_name"),
            characterRealm = rs.getString("character_realm"),
            characterRegion = rs.getString("character_region"),
            characterClass = rs.getString("character_class"),
            characterRole = rs.getString("character_role"),
            characterGuest = rs.getObject("character_guest") as? Boolean,
            status = rs.getString("status"),
            comment = rs.getString("comment"),
            selected = rs.getBoolean("selected")
        )
    }
}
