package com.edgerush.lootman.infrastructure.persistence.jpa

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface LootAwardJpaRepository : JpaRepository<LootAwardEntity, Long>

@Repository
class JpaLootAwardRepositoryImpl(
    private val jpaRepository: LootAwardJpaRepository
) : LootAwardRepository {

    override fun findById(id: LootAwardId): LootAward? {
        return jpaRepository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun findByRaiderId(raiderId: RaiderId): List<LootAward> {
        return jpaRepository.findAll().map { it.toDomain() }.take(100)
    }

    override fun findByGuildId(guildId: GuildId): List<LootAward> {
        return jpaRepository.findAll().map { it.toDomain() }.take(500)
    }

    override fun save(lootAward: LootAward): LootAward {
        throw UnsupportedOperationException("Save not yet implemented")
    }

    override fun delete(id: LootAwardId) {
        jpaRepository.deleteById(id.value)
    }
}
