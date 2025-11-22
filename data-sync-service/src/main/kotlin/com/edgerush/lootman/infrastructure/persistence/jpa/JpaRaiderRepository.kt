package com.edgerush.lootman.infrastructure.persistence.jpa

import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for RaiderEntity.
 */
interface RaiderJpaRepository : JpaRepository<RaiderEntity, Long> {
    fun findByStatus(status: String): List<RaiderEntity>
    fun findByCharacterNameAndRealm(characterName: String, realm: String): RaiderEntity?
}

/**
 * Implementation of RaiderRepository using JPA.
 */
@Repository
class JpaRaiderRepositoryImpl(
    private val jpaRepository: RaiderJpaRepository
) : RaiderRepository {

    override fun findById(id: RaiderId): Raider? {
        return jpaRepository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun findByGuildId(guildId: GuildId): List<Raider> {
        return jpaRepository.findByStatus("active").map { it.toDomain() }
    }

    override fun findByCharacterNameAndRealm(characterName: String, realm: String): Raider? {
        return jpaRepository.findByCharacterNameAndRealm(characterName, realm)?.toDomain()
    }

    override fun save(raider: Raider): Raider {
        throw UnsupportedOperationException("Save not yet implemented")
    }

    override fun delete(id: RaiderId) {
        jpaRepository.deleteById(id.value)
    }
}
