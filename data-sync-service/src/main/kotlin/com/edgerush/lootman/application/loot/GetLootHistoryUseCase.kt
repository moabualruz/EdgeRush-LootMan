package com.edgerush.lootman.application.loot

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId

import org.springframework.stereotype.Service

/**
 * Use case for retrieving loot history.
 *
 * Provides methods to query loot awards by guild or raider.
 */
@Service
class GetLootHistoryUseCase(
    private val lootAwardRepository: LootAwardRepository,
) {
    /**
     * Gets loot history for a guild.
     *
     * @param guildId The guild to query
     * @param limit Maximum number of awards to return (currently not used)
     * @return List of loot awards for the guild
     */
    fun getGuildHistory(
        guildId: GuildId,
        limit: Int = 100,
    ): Result<List<LootAward>> =
        runCatching {
            lootAwardRepository.findByGuildId(guildId)
        }

    /**
     * Gets loot history for a raider.
     *
     * @param raiderId The raider to query
     * @return List of loot awards for the raider
     */
    fun getRaiderHistory(raiderId: RaiderId): Result<List<LootAward>> =
        runCatching {
            lootAwardRepository.findByRaiderId(raiderId)
        }
}

