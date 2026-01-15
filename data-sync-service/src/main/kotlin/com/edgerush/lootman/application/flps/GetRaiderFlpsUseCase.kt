package com.edgerush.lootman.application.flps

import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service

/**
 * Use case for getting FLPS score for a specific raider and item.
 *
 * This use case combines data assembly and FLPS calculation into a single
 * operation suitable for direct queries from API layers (REST, GraphQL).
 */
@Service
class GetRaiderFlpsUseCase(
    private val flpsDataAssembler: FlpsDataAssemblerService,
    private val componentCalculator: FlpsComponentCalculator,
    private val calculateFlpsScoreUseCase: CalculateFlpsScoreUseCase,
) {
    /**
     * Get FLPS score for a raider and item.
     *
     * @param query The query with raider, guild, and item identifiers
     * @return Result containing the FLPS calculation or error
     */
    fun execute(query: GetRaiderFlpsQuery): Result<FlpsCalculationResult> =
        runCatching {
            // Assemble all raider data from the database
            val allRaiderData = flpsDataAssembler.assembleFlpsData(query.guildId)

            // Find the specific raider
            val raiderData =
                allRaiderData.find { it.raider.id == query.raiderId }
                    ?: throw NoSuchElementException("Raider not found with id: ${query.raiderId.value}")

            // Calculate all component scores
            val acs = componentCalculator.calculateACS(raiderData.attendance)
            val mas = componentCalculator.calculateMAS()
            val eps = componentCalculator.calculateEPS(raiderData.gear)
            val uv = componentCalculator.calculateUV(raiderData.wishlist, query.itemId)
            val tb = componentCalculator.calculateTierBonus(raiderData.gear)
            val rm = componentCalculator.calculateRoleMultiplier(raiderData.raider.role)
            val rdf = componentCalculator.calculateRDF(raiderData.lootHistory, raiderData.activeBans)

            // Create command and calculate FLPS
            val command =
                CalculateFlpsScoreCommand(
                    guildId = query.guildId,
                    raiderId = query.raiderId,
                    itemId = query.itemId,
                    acs = acs,
                    mas = mas,
                    eps = eps,
                    uv = uv,
                    tb = tb,
                    rm = rm,
                    rdf = rdf,
                )

            calculateFlpsScoreUseCase.execute(command).getOrThrow()
        }
}

/**
 * Query for getting FLPS score for a specific raider and item.
 */
data class GetRaiderFlpsQuery(
    val guildId: GuildId,
    val raiderId: RaiderId,
    val itemId: ItemId,
)
