package com.edgerush.lootman.infrastructure.persistence.repository

import com.edgerush.datasync.repository.LootBanRepository as SpringLootBanRepository
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.infrastructure.persistence.mapper.LootBanMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * JDBC implementation of LootBanRepository.
 * Bridges the domain layer with the infrastructure persistence layer.
 */
@Repository
@Primary
class JdbcLootBanRepository(
    private val springRepository: SpringLootBanRepository,
    private val mapper: LootBanMapper,
) : LootBanRepository {
    override fun findById(id: LootBanId): LootBan? {
        val entityId = id.value.toLongOrNull() ?: return null
        return springRepository.findById(entityId)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }

    override fun findActiveByRaiderId(
        raiderId: RaiderId,
        guildId: GuildId,
    ): List<LootBan> {
        val currentTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
        val entity =
            springRepository.findActiveBanForCharacter(
                guildId = guildId.value,
                characterName = raiderId.value,
                currentTime = currentTime,
            )

        return entity?.let { listOf(mapper.toDomain(it)) } ?: emptyList()
    }

    override fun save(lootBan: LootBan): LootBan {
        val entity = mapper.toEntity(lootBan)
        val saved = springRepository.save(entity)
        return mapper.toDomain(saved)
    }

    override fun delete(id: LootBanId) {
        val entityId = id.value.toLongOrNull() ?: return
        springRepository.deleteById(entityId)
    }
}
