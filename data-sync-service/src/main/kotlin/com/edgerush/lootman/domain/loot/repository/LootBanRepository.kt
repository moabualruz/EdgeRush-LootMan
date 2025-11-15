package com.edgerush.lootman.domain.loot.repository

import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId

/**
 * Repository interface for LootBan aggregate.
 *
 * This is a domain interface that will be implemented by the infrastructure layer.
 */
interface LootBanRepository {
    fun findById(id: LootBanId): LootBan?

    fun findActiveByRaiderId(
        raiderId: RaiderId,
        guildId: GuildId,
    ): List<LootBan>

    fun save(lootBan: LootBan): LootBan

    fun delete(id: LootBanId)
}
