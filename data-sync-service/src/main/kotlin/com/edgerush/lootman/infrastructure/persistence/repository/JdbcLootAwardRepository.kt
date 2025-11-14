package com.edgerush.lootman.infrastructure.persistence.repository

import com.edgerush.datasync.repository.LootAwardRepository as SpringLootAwardRepository
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.infrastructure.persistence.mapper.LootAwardMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of LootAwardRepository.
 * Bridges the domain layer with the infrastructure persistence layer.
 */
@Repository
@Primary
class JdbcLootAwardRepository(
    private val springRepository: SpringLootAwardRepository,
    private val mapper: LootAwardMapper,
) : LootAwardRepository {
    override fun findById(id: LootAwardId): LootAward? {
        val entityId = id.value.toLongOrNull() ?: return null
        return springRepository.findById(entityId)
            .map { mapper.toDomain(it, GuildId("unknown")) }
            .orElse(null)
    }

    override fun findByRaiderId(raiderId: RaiderId): List<LootAward> {
        val raiderIdLong = raiderId.value.toLongOrNull() ?: return emptyList()
        return springRepository.findByRaiderId(raiderIdLong)
            .map { mapper.toDomain(it, GuildId("unknown")) }
    }

    override fun findByGuildId(guildId: GuildId): List<LootAward> {
        // Note: Legacy table doesn't have guild_id column
        // This returns all loot awards - filtering by guild would need to be done at application layer
        return springRepository.findAll()
            .map { mapper.toDomain(it, guildId) }
            .toList()
    }

    override fun save(lootAward: LootAward): LootAward {
        val entity = mapper.toEntity(lootAward)
        val saved = springRepository.save(entity)
        return mapper.toDomain(saved, lootAward.guildId)
    }

    override fun delete(id: LootAwardId) {
        val entityId = id.value.toLongOrNull() ?: return
        springRepository.deleteById(entityId)
    }
}
