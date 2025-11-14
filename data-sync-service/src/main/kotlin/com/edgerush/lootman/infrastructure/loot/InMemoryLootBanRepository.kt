package com.edgerush.lootman.infrastructure.loot

import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of LootBanRepository.
 *
 * This implementation is suitable for testing and development.
 * For production, use a database-backed implementation.
 */
@Repository
class InMemoryLootBanRepository : LootBanRepository {
    private val storage = ConcurrentHashMap<LootBanId, LootBan>()

    override fun findById(id: LootBanId): LootBan? = storage[id]

    override fun findActiveByRaiderId(
        raiderId: RaiderId,
        guildId: GuildId,
    ): List<LootBan> {
        val now = Instant.now()
        return storage.values
            .filter { it.raiderId == raiderId && it.guildId == guildId }
            .filter { it.isActive(now) }
    }

    override fun save(lootBan: LootBan): LootBan {
        storage[lootBan.id] = lootBan
        return lootBan
    }

    override fun delete(id: LootBanId) {
        storage.remove(id)
    }
}

