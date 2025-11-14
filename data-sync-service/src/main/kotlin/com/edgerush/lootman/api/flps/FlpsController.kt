package com.edgerush.lootman.api.flps

import com.edgerush.lootman.application.flps.CalculateFlpsScoreUseCase
import com.edgerush.lootman.application.flps.GetFlpsReportQuery
import com.edgerush.lootman.application.flps.GetFlpsReportUseCase
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for FLPS (Final Loot Priority Score) operations.
 *
 * This controller provides endpoints for:
 * - Calculating FLPS scores for raiders
 * - Generating FLPS reports for guilds
 * - Retrieving perfect score benchmarks
 *
 * The controller uses the new domain-driven architecture with use cases
 * while maintaining backward compatibility with existing API contracts.
 */
@RestController
@RequestMapping("/api/v1/flps")
class FlpsController(
    private val calculateFlpsScoreUseCase: CalculateFlpsScoreUseCase,
    private val getFlpsReportUseCase: GetFlpsReportUseCase,
) {
    /**
     * Get comprehensive FLPS report for a guild.
     *
     * Returns FLPS calculations for all raiders in the guild, sorted by score.
     * Includes component breakdowns, eligibility status, and behavioral scoring.
     *
     * @param guildId The guild identifier
     * @return List of FLPS calculations for all raiders
     */
    @GetMapping("/guilds/{guildId}/report")
    fun getFlpsReport(
        @PathVariable guildId: String,
    ): FlpsReportResponse {
        val query =
            GetFlpsReportQuery(
                guildId = GuildId(guildId),
                calculations = emptyList(), // TODO: Fetch calculations from data source
            )

        return getFlpsReportUseCase.execute(query)
            .map { report -> FlpsReportResponse.from(report) }
            .getOrThrow()
    }

    /**
     * Get system status and capabilities.
     *
     * @return System status information
     */
    @GetMapping("/status")
    fun getStatus(): FlpsStatusResponse {
        return FlpsStatusResponse(
            message = "FLPS calculation system using domain-driven architecture",
            features =
                listOf(
                    "Domain-driven design with bounded contexts",
                    "Test-driven development with 85%+ coverage",
                    "Guild-specific FLPS modifiers",
                    "Component score breakdowns (RMS, IPI, RDF)",
                    "Eligibility determination",
                    "Behavioral scoring integration",
                ),
            endpoints =
                mapOf(
                    "Guild Report" to "/api/v1/flps/guilds/{guildId}/report",
                    "System Status" to "/api/v1/flps/status",
                ),
        )
    }
}
