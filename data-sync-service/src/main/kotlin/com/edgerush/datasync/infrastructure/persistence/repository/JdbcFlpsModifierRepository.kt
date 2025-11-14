package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.flps.repository.FlpsConfiguration
import com.edgerush.datasync.domain.flps.repository.FlpsModifierRepository
import com.edgerush.datasync.infrastructure.persistence.mapper.FlpsModifierMapper
import com.edgerush.datasync.repository.FlpsGuildModifierRepository
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of FlpsModifierRepository.
 * Bridges the domain layer with the infrastructure persistence layer.
 */
@Repository
class JdbcFlpsModifierRepository(
    private val springRepository: FlpsGuildModifierRepository,
    private val mapper: FlpsModifierMapper
) : FlpsModifierRepository {

    override fun findByGuildId(guildId: String): FlpsConfiguration {
        val entities = springRepository.findByGuildId(guildId)
        return mapper.toDomain(entities)
    }

    override fun save(guildId: String, configuration: FlpsConfiguration) {
        // Delete existing modifiers for this guild
        springRepository.deleteByGuildId(guildId)
        
        // Convert configuration to entities and save
        val entities = mapper.toEntities(guildId, configuration)
        entities.forEach { springRepository.save(it) }
    }
}
