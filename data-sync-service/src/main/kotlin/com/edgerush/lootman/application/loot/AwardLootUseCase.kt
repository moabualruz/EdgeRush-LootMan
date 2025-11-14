package com.edgerush.lootman.application.loot

import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.loot.service.LootDistributionService
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.LootBanActiveException
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Use case for awarding loot to a raider.
 *
 * This orchestrates the loot award process including:
 * - Checking for active loot bans
 * - Validating eligibility
 * - Creating and persisting the loot award
 */
@Service
class AwardLootUseCase(
    private val lootAwardRepository: LootAwardRepository,
    private val lootBanRepository: LootBanRepository,
    private val lootDistributionService: LootDistributionService,
) {
    /**
     * Executes the loot award process.
     *
     * @param command The award parameters
     * @return Result containing LootAward or error
     */
    fun execute(command: AwardLootCommand): Result<LootAward> =
        runCatching {
            val now = Instant.now()

            // Check for active loot bans
            val activeBans = lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId)

            // Validate eligibility
            if (!lootDistributionService.isEligibleForLoot(command.raiderId, activeBans, now)) {
                throw LootBanActiveException(command.raiderId, activeBans)
            }

            // Create loot award
            val lootAward =
                LootAward.create(
                    itemId = command.itemId,
                    raiderId = command.raiderId,
                    guildId = command.guildId,
                    flpsScore = command.flpsScore,
                    tier = command.tier,
                )

            // Persist
            lootAwardRepository.save(lootAward)
        }
}

/**
 * Command for awarding loot.
 */
data class AwardLootCommand(
    val itemId: ItemId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val flpsScore: FlpsScore,
    val tier: LootTier,
)
