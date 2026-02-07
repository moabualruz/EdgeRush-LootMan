package com.edgerush.lootman.application.loot

import com.edgerush.lootman.application.flps.FlpsComponentCalculator
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.flps.repository.RaiderPerformanceRepository
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.loot.service.LootDistributionService
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.InsufficientPerformanceException
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.LootBanActiveException
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Use case for awarding loot to a raider.
 *
 * This orchestrates the loot award process including:
 * - Checking for active loot bans
 * - Validating eligibility
 * - Validating MAS (Mechanical Adherence Score) threshold (EdgeRush Upgrade)
 * - Creating and persisting the loot award
 */
@Service
class AwardLootUseCase(
    private val lootAwardRepository: LootAwardRepository,
    private val lootBanRepository: LootBanRepository,
    private val lootDistributionService: LootDistributionService,
    private val raiderPerformanceRepository: RaiderPerformanceRepository? = null,
    private val flpsComponentCalculator: FlpsComponentCalculator? = null,
) {
    companion object {
        /** Default MAS threshold (0.5 = 50%). Configurable per guild in future. */
        const val DEFAULT_MAS_THRESHOLD = 0.5

        /** Performance data lookback period in days */
        const val PERFORMANCE_LOOKBACK_DAYS = 30L
    }

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

            // EdgeRush Upgrade: Contextual Loot Distribution
            // Validate MAS threshold unless bypassed (officer override)
            if (!command.bypassMasCheck && raiderPerformanceRepository != null && flpsComponentCalculator != null) {
                validateMasThreshold(command, now)
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

    /**
     * Validates that the raider's MAS meets the configured threshold.
     *
     * @throws InsufficientPerformanceException if MAS is below threshold
     */
    private fun validateMasThreshold(
        command: AwardLootCommand,
        now: Instant,
    ) {
        val periodStart = now.minus(PERFORMANCE_LOOKBACK_DAYS, ChronoUnit.DAYS)
        val periodEnd = now

        val performanceData =
            raiderPerformanceRepository!!.findByRaiderAndPeriod(
                raiderId = command.raiderId,
                guildId = command.guildId,
                startDate = periodStart,
                endDate = periodEnd,
            )

        // If no performance data exists, allow the award (new raiders get a pass)
        if (performanceData == null) {
            return
        }

        val mas = flpsComponentCalculator!!.calculateMAS(performanceData)
        val threshold = DEFAULT_MAS_THRESHOLD

        if (mas.value < threshold) {
            throw InsufficientPerformanceException(
                raiderId = command.raiderId,
                actualScore = mas,
                requiredThreshold = threshold,
            )
        }
    }
}

/**
 * Command for awarding loot.
 *
 * @property bypassMasCheck If true, skip MAS threshold validation (officer override)
 */
data class AwardLootCommand(
    val itemId: ItemId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val flpsScore: FlpsScore,
    val tier: LootTier,
    val bypassMasCheck: Boolean = false,
)
