package com.edgerush.lootman.application.loot

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service

/**
 * Use case for retrieving loot history.
 *
 * This handles:
 * - Getting loot history by guild
 * - Getting loot history by raider
 * - Filtering by active/revoked status
 */
@Service
class GetLootHistoryUseCase(
    private val lootAwardRepository: LootAwardRepository,
) {
    /**
     * Gets loot history for a guild.
     *
     * @param query The query parameters
     * @return Result containing list of loot awards or error
     */
    fun getByGuild(query: GetLootHistoryByGuildQuery): Result<List<LootAward>> =
        runCatching {
            val awards = lootAwardRepository.findByGuildId(query.guildId)

            if (query.activeOnly) {
                awards.filter { it.isActive() }
            } else {
                awards
            }
        }

    /**
     * Gets loot history for a raider.
     *
     * @param query The query parameters
     * @return Result containing list of loot awards or error
     */
    fun getByRaider(query: GetLootHistoryByRaiderQuery): Result<List<LootAward>> =
        runCatching {
            val awards = lootAwardRepository.findByRaiderId(query.raiderId)

            if (query.activeOnly) {
                awards.filter { it.isActive() }
            } else {
                awards
            }
        }
}

/**
 * Query for getting loot history by guild.
 */
data class GetLootHistoryByGuildQuery(
    val guildId: GuildId,
    val activeOnly: Boolean = false,
)

/**
 * Query for getting loot history by raider.
 */
data class GetLootHistoryByRaiderQuery(
    val raiderId: RaiderId,
    val activeOnly: Boolean = false,
)
