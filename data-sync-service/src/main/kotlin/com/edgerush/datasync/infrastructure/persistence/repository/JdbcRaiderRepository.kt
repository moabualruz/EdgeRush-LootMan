package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.shared.model.Raider
import com.edgerush.datasync.domain.shared.model.RaiderId
import com.edgerush.datasync.domain.shared.model.RaiderStatus
import com.edgerush.datasync.domain.shared.repository.RaiderRepository as DomainRaiderRepository
import com.edgerush.datasync.infrastructure.persistence.mapper.RaiderMapper
import com.edgerush.datasync.repository.RaiderRepository as SpringRaiderRepository
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of RaiderRepository.
 */
@Repository
class JdbcRaiderRepository(
    private val springRepository: SpringRaiderRepository,
    private val mapper: RaiderMapper
) : DomainRaiderRepository {
    
    override fun findById(id: RaiderId): Raider? {
        return springRepository.findById(id.value)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }
    
    override fun findByCharacterNameAndRealm(characterName: String, realm: String): Raider? {
        return springRepository.findByCharacterNameAndRealm(characterName, realm)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }
    
    override fun findByWowauditId(wowauditId: Long): Raider? {
        return springRepository.findByWowauditId(wowauditId)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }
    
    override fun findAll(): List<Raider> {
        return springRepository.findAll()
            .map { mapper.toDomain(it) }
    }
    
    override fun findAllActive(): List<Raider> {
        return springRepository.findAll()
            .map { mapper.toDomain(it) }
            .filter { it.status == RaiderStatus.ACTIVE }
    }
    
    override fun save(raider: Raider): Raider {
        val entity = mapper.toEntity(raider)
        val saved = springRepository.save(entity)
        return mapper.toDomain(saved)
    }
    
    override fun deleteById(id: RaiderId) {
        springRepository.deleteById(id.value)
    }
}
