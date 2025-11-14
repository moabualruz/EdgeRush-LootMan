package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.raids.model.GuildId
import com.edgerush.datasync.domain.raids.model.Raid
import com.edgerush.datasync.domain.raids.model.RaidId
import com.edgerush.datasync.domain.raids.repository.RaidRepository
import com.edgerush.datasync.infrastructure.persistence.mapper.RaidMapper
import com.edgerush.datasync.repository.RaidEncounterRepository
import com.edgerush.datasync.repository.RaidSignupRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import com.edgerush.datasync.repository.RaidRepository as SpringRaidRepository

/**
 * Spring Data JDBC implementation of RaidRepository.
 * Handles persistence of Raid aggregates including encounters and signups.
 */
@Repository
class JdbcRaidRepository(
    private val springRaidRepository: SpringRaidRepository,
    private val encounterRepository: RaidEncounterRepository,
    private val signupRepository: RaidSignupRepository,
    private val mapper: RaidMapper
) : RaidRepository {
    
    override fun findById(id: RaidId): Raid? {
        val entity = springRaidRepository.findById(id.value).orElse(null) ?: return null
        val encounters = encounterRepository.findByRaidId(id.value)
        val signups = signupRepository.findByRaidId(id.value)
        
        // TODO: Get actual guild ID from database or context
        return mapper.toDomain(entity, "unknown-guild", encounters, signups)
    }
    
    override fun findByGuildId(guildId: GuildId): List<Raid> {
        // Note: Current schema doesn't have guild_id column
        // This is a limitation that needs to be addressed
        return springRaidRepository.findAll().map { entity ->
            val encounters = encounterRepository.findByRaidId(entity.raidId)
            val signups = signupRepository.findByRaidId(entity.raidId)
            mapper.toDomain(entity, guildId.value, encounters, signups)
        }
    }
    
    override fun findByGuildIdAndDate(guildId: GuildId, date: LocalDate): List<Raid> {
        return springRaidRepository.findByDate(date).map { entity ->
            val encounters = encounterRepository.findByRaidId(entity.raidId)
            val signups = signupRepository.findByRaidId(entity.raidId)
            mapper.toDomain(entity, guildId.value, encounters, signups)
        }
    }
    
    @Transactional
    override fun save(raid: Raid): Raid {
        val entity = mapper.toEntity(raid)
        springRaidRepository.save(entity)
        
        // Delete and re-insert encounters and signups
        encounterRepository.deleteByRaidId(entity.raidId)
        signupRepository.deleteByRaidId(entity.raidId)
        
        val encounterEntities = mapper.encounterMapper.toEntities(raid.getEncounters(), entity.raidId)
        val signupEntities = mapper.signupMapper.toEntities(raid.getSignups(), entity.raidId)
        
        encounterRepository.saveAll(encounterEntities)
        signupRepository.saveAll(signupEntities)
        
        return raid
    }
    
    @Transactional
    override fun delete(id: RaidId) {
        // Cascading deletes handled by database foreign keys
        springRaidRepository.deleteById(id.value)
    }
}
