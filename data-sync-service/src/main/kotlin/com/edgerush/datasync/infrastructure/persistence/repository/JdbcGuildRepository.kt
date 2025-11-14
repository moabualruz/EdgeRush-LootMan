package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.shared.model.Guild
import com.edgerush.datasync.domain.shared.model.GuildId
import com.edgerush.datasync.domain.shared.repository.GuildRepository as DomainGuildRepository
import com.edgerush.datasync.infrastructure.persistence.mapper.GuildMapper
import com.edgerush.datasync.repository.GuildConfigurationRepository
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of GuildRepository.
 */
@Repository
class JdbcGuildRepository(
    private val springRepository: GuildConfigurationRepository,
    private val mapper: GuildMapper
) : DomainGuildRepository {
    
    override fun findById(id: GuildId): Guild? {
        return springRepository.findByGuildId(id.value)
            ?.let { mapper.toDomain(it) }
    }
    
    override fun findActiveById(id: GuildId): Guild? {
        return springRepository.findByGuildIdAndIsActive(id.value, true)
            ?.let { mapper.toDomain(it) }
    }
    
    override fun findAll(): List<Guild> {
        return springRepository.findAll()
            .map { mapper.toDomain(it) }
    }
    
    override fun findAllActive(): List<Guild> {
        return springRepository.findByIsActive(true)
            .map { mapper.toDomain(it) }
    }
    
    override fun save(guild: Guild): Guild {
        val entity = mapper.toEntity(guild)
        val saved = springRepository.save(entity)
        return mapper.toDomain(saved)
    }
    
    override fun deleteById(id: GuildId) {
        springRepository.findByGuildId(id.value)?.let {
            springRepository.delete(it)
        }
    }
}
