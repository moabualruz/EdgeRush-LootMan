package com.edgerush.lootman.application.flps

import com.edgerush.lootman.domain.shared.GuildId

/**
 * Use case for generating FLPS reports.
 *
 * This use case aggregates multiple FLPS calculations and sorts them
 * by score for easy comparison and decision-making.
 */
class GetFlpsReportUseCase {
    /**
     * Executes the report generation.
     *
     * @param query The report query with calculations to aggregate
     * @return Result containing FlpsReport or error
     */
    fun execute(query: GetFlpsReportQuery): Result<FlpsReport> =
        runCatching {
            // Sort calculations by FLPS score descending
            val sortedCalculations = query.calculations.sortedByDescending { it.flps.value }

            FlpsReport(
                guildId = query.guildId,
                calculations = sortedCalculations,
            )
        }
}

/**
 * Query for getting FLPS report.
 */
data class GetFlpsReportQuery(
    val guildId: GuildId,
    val calculations: List<FlpsCalculationResult>,
)

/**
 * FLPS report containing sorted calculations.
 */
data class FlpsReport(
    val guildId: GuildId,
    val calculations: List<FlpsCalculationResult>,
)
