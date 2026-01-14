package com.edgerush.lootman.api.graphql.query

import com.edgerush.lootman.application.flps.FlpsCalculationResult
import com.edgerush.lootman.application.flps.FlpsReport
import com.edgerush.lootman.application.flps.GetFlpsReportQuery
import com.edgerush.lootman.application.flps.GetFlpsReportUseCase
import com.edgerush.lootman.application.flps.GetRaiderFlpsQuery
import com.edgerush.lootman.application.flps.GetRaiderFlpsUseCase
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component

/**
 * GraphQL Query resolver for FLPS (Final Loot Priority Score) operations.
 *
 * Exposes FLPS queries through GraphQL, delegating to the application layer use cases.
 * Provides both individual score calculations and aggregated reports.
 */
@Component
class FlpsQueryResolver(
    private val getRaiderFlpsUseCase: GetRaiderFlpsUseCase,
    private val getFlpsReportUseCase: GetFlpsReportUseCase,
) : Query {

    /**
     * Calculate FLPS score for a specific raider and item.
     *
     * @param guildId The guild ID
     * @param raiderId The raider ID
     * @param itemId The item ID
     * @return The FLPS score with breakdown if calculation succeeds, null if raider not found
     * @throws RuntimeException for non-NotFound errors
     */
    fun flpsScore(guildId: String, raiderId: String, itemId: String): FlpsScoreType? {
        val query = GetRaiderFlpsQuery(
            guildId = GuildId(guildId),
            raiderId = RaiderId(raiderId.toLong()),
            itemId = ItemId(itemId.toLong()),
        )
        return getRaiderFlpsUseCase.execute(query)
            .map { it.toGraphQLType() }
            .getOrElse { exception ->
                if (exception is NoSuchElementException) {
                    null
                } else {
                    throw exception
                }
            }
    }

    /**
     * Get FLPS report for a guild.
     *
     * @param guildId The guild ID
     * @return The FLPS report with all raider scores
     * @throws RuntimeException on errors
     */
    fun flpsReport(guildId: String): FlpsReportType {
        val query = GetFlpsReportQuery(
            guildId = GuildId(guildId),
            calculations = emptyList(), // Calculations are populated by the use case or upstream
        )
        return getFlpsReportUseCase.execute(query)
            .map { it.toGraphQLType() }
            .getOrThrow()
    }
}

/**
 * GraphQL type representing an FLPS score with full breakdown.
 */
data class FlpsScoreType(
    val value: Double,
    val raiderId: String,
    val itemId: String,
    val eligible: Boolean,
    val breakdown: FlpsBreakdownType,
)

/**
 * GraphQL type representing the breakdown of an FLPS score.
 *
 * Contains all component scores that contribute to the final FLPS value:
 * - RMS components: ACS (Attendance), MAS (Mechanical), EPS (External Preparation)
 * - IPI components: UV (Upgrade Value), TB (Tier Bonus), RM (Role Multiplier)
 * - Time factor: RDF (Recency Decay Factor)
 */
data class FlpsBreakdownType(
    val acs: Double,  // Attendance Commitment Score
    val mas: Double,  // Mechanical Adherence Score
    val eps: Double,  // External Preparation Score
    val rms: Double,  // Raider Merit Score (aggregated)
    val uv: Double,   // Upgrade Value
    val tb: Double,   // Tier Bonus
    val rm: Double,   // Role Multiplier
    val ipi: Double,  // Item Priority Index (aggregated)
    val rdf: Double,  // Recency Decay Factor
)

/**
 * GraphQL type representing an FLPS report for a guild.
 */
data class FlpsReportType(
    val guildId: String,
    val scores: List<FlpsScoreType>,
)

/**
 * Extension function to convert FlpsCalculationResult to GraphQL FlpsScoreType.
 */
private fun FlpsCalculationResult.toGraphQLType(): FlpsScoreType = FlpsScoreType(
    value = this.flps.value,
    raiderId = this.raiderId.value.toString(),
    itemId = this.itemId.value.toString(),
    eligible = this.eligible,
    breakdown = FlpsBreakdownType(
        acs = this.acs.value,
        mas = this.mas.value,
        eps = this.eps.value,
        rms = this.rms.value,
        uv = this.uv.value,
        tb = this.tb.value,
        rm = this.rm.value,
        ipi = this.ipi.value,
        rdf = this.rdf.value,
    ),
)

/**
 * Extension function to convert FlpsReport to GraphQL FlpsReportType.
 */
private fun FlpsReport.toGraphQLType(): FlpsReportType = FlpsReportType(
    guildId = this.guildId.value,
    scores = this.calculations.map { it.toGraphQLType() },
)
