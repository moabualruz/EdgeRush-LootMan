package com.edgerush.lootman.api.graphql.query

import com.edgerush.lootman.application.loot.GetLootHistoryByGuildQuery
import com.edgerush.lootman.application.loot.GetLootHistoryByRaiderQuery
import com.edgerush.lootman.application.loot.GetLootHistoryUseCase
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * GraphQL Query resolver for Loot operations.
 *
 * Exposes loot queries through GraphQL, delegating to the application layer use cases.
 * Provides access to loot awards history by guild or raider.
 */
@Component
class LootQueryResolver(
    private val getLootHistoryUseCase: GetLootHistoryUseCase,
) : Query {

    /**
     * Get loot awards for a guild.
     *
     * @param guildId The guild ID
     * @param activeOnly If true, only return active (non-revoked) awards
     * @return List of loot awards for the guild
     * @throws RuntimeException on errors
     */
    fun lootAwards(guildId: String, activeOnly: Boolean = false): List<LootAwardType> {
        val query = GetLootHistoryByGuildQuery(
            guildId = GuildId(guildId),
            activeOnly = activeOnly,
        )
        return getLootHistoryUseCase.getByGuild(query)
            .map { awards -> awards.map { it.toGraphQLType() } }
            .getOrThrow()
    }

    /**
     * Get loot history for a specific raider.
     *
     * @param raiderId The raider ID
     * @param activeOnly If true, only return active (non-revoked) awards
     * @return List of loot awards for the raider
     * @throws RuntimeException on errors
     */
    fun lootHistory(raiderId: String, activeOnly: Boolean = false): List<LootAwardType> {
        val query = GetLootHistoryByRaiderQuery(
            raiderId = RaiderId(raiderId.toLong()),
            activeOnly = activeOnly,
        )
        return getLootHistoryUseCase.getByRaider(query)
            .map { awards -> awards.map { it.toGraphQLType() } }
            .getOrThrow()
    }
}

/**
 * GraphQL type representing a loot award.
 */
data class LootAwardType(
    val id: String,
    val raiderId: String,
    val itemId: String,
    val guildId: String,
    val tier: LootTier,
    val flpsScore: Double,
    val awardedAt: Instant,
    val isActive: Boolean,
)

/**
 * Extension function to convert domain LootAward to GraphQL LootAwardType.
 */
private fun LootAward.toGraphQLType(): LootAwardType = LootAwardType(
    id = this.id.value,
    raiderId = this.raiderId.value.toString(),
    itemId = this.itemId.value.toString(),
    guildId = this.guildId.value,
    tier = this.tier,
    flpsScore = this.flpsScore.value,
    awardedAt = this.awardedAt,
    isActive = this.isActive(),
)
