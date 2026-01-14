package com.edgerush.lootman.api.graphql.mutation

import com.edgerush.lootman.api.graphql.query.LootAwardType
import com.edgerush.lootman.application.loot.AwardLootCommand
import com.edgerush.lootman.application.loot.AwardLootUseCase
import com.edgerush.lootman.application.loot.RevokeLootAwardCommand
import com.edgerush.lootman.application.loot.RevokeLootAwardUseCase
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.expediagroup.graphql.server.operations.Mutation
import org.springframework.stereotype.Component

/**
 * GraphQL Mutation resolver for Loot operations.
 *
 * Exposes loot mutations through GraphQL, delegating to the application layer use cases.
 * Provides award and revoke operations for loot.
 */
@Component
class LootMutationResolver(
    private val awardLootUseCase: AwardLootUseCase,
    private val revokeLootAwardUseCase: RevokeLootAwardUseCase,
) : Mutation {

    /**
     * Award loot to a raider.
     *
     * @param input The loot award input
     * @return The created loot award
     * @throws RuntimeException on errors (including active loot bans)
     */
    fun awardLoot(input: AwardLootInput): LootAwardType {
        val command = AwardLootCommand(
            itemId = ItemId(input.itemId.toLong()),
            raiderId = RaiderId(input.raiderId.toLong()),
            guildId = GuildId(input.guildId),
            flpsScore = FlpsScore.of(input.flpsScore),
            tier = LootTier.valueOf(input.tier.uppercase()),
        )
        return awardLootUseCase.execute(command)
            .map { it.toGraphQLType() }
            .getOrThrow()
    }

    /**
     * Revoke a loot award.
     *
     * @param awardId The loot award ID to revoke
     * @return True if revocation was successful
     * @throws RuntimeException on errors
     */
    fun revokeLootAward(awardId: String): Boolean {
        val command = RevokeLootAwardCommand(awardId = awardId)
        return revokeLootAwardUseCase.execute(command)
            .map { true }
            .getOrThrow()
    }
}

/**
 * Input type for awarding loot.
 */
data class AwardLootInput(
    val itemId: String,
    val raiderId: String,
    val guildId: String,
    val flpsScore: Double,
    val tier: String,
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
