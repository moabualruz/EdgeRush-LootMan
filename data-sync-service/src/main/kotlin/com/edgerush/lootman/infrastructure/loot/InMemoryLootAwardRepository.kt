package com.edgerush.lootman.infrastructure.loot

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of LootAwardRepository.
 */
@Repository
class InMemoryLootAwardRepository : LootAwardRepository {
    private val storage = ConcurrentHashMap<LootAwardId, LootAward>()

    override fun findById(id: LootAwardId): LootAward? = storage[id]

    override fun findByRaiderId(raiderId: RaiderId): List<LootAward> = storage.values.filter { it.raiderId == raiderId }

    override fun findByGuildId(guildId: GuildId): List<LootAward> = storage.values.filter { it.guildId == guildId }

    override fun save(lootAward: LootAward): LootAward {
        storage[lootAward.id] = lootAward
        return lootAward
    }

    override fun delete(id: LootAwardId) {
        storage.remove(id)
    }
}
