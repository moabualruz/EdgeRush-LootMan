package com.edgerush.lootman.application.loot

import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId

import org.springframework.stereotype.Service

/**
 * Use case for awarding loot to a raider.
 */
@Service
class AwardLootUseCase(
    private val lootAwardRepository: LootAwardRepository,
) {
    fun execute(command: AwardLootCommand): Result<LootAward> =
        runCatching {
            val lootAward =
                LootAward.create(
                    itemId = command.itemId,
                    raiderId = command.raiderId,
                    guildId = command.guildId,
                    flpsScore = command.flpsScore,
                    tier = command.tier,
                )

            lootAwardRepository.save(lootAward)
        }
}

data class AwardLootCommand(
    val itemId: ItemId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val flpsScore: FlpsScore,
    val tier: LootTier,
)
