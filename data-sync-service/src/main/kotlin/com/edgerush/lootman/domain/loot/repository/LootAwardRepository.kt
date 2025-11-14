package com.edgerush.lootman.domain.loot.repository

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId

/**
 * Repository interface for LootAward aggregate.
 */
interface LootAwardRepository {
    fun findById(id: LootAwardId): LootAward?

    fun findByRaiderId(raiderId: RaiderId): List<LootAward>

    fun findByGuildId(guildId: GuildId): List<LootAward>

    fun save(lootAward: LootAward): LootAward

    fun delete(id: LootAwardId)
}
