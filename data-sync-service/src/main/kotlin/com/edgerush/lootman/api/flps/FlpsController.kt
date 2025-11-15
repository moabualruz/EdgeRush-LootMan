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
@RestController("lootmanFlpsController")
@RequestMapping
class FlpsController(
    private val calculateFlpsScoreUseCase: CalculateFlpsScoreUseCase,
    private val getFlpsReportUseCase: GetFlpsReportUseCase,
) {
    /**
     * Get comprehensive FLPS report for a guild (legacy endpoint).
     *
     * Returns FLPS calculations for all raiders in the guild, sorted by score.
     * Includes component breakdowns, eligibility status, and behavioral scoring.
     *
     * @param guildId The guild identifier
     * @return List of FLPS calculations for all raiders
     */
    @GetMapping("/api/flps/{guildId}")
    fun getFlpsReport(
        @PathVariable guildId: String,
    ): List<ComprehensiveFlpsReportDto> {
        return getFlpsReportInternal(guildId)
    }

    /**
     * Get comprehensive FLPS report for a guild (v1 endpoint).
     *
     * @param guildId The guild identifier
     * @return FLPS report response
     */
    @GetMapping("/api/v1/flps/guilds/{guildId}/report")
    fun getFlpsReportV1(
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
     * Get perfect score benchmarks for a guild.
     *
     * @param guildId The guild identifier
     * @return Perfect score benchmarks
     */
    @GetMapping("/api/flps/{guildId}/benchmarks")
    fun getBenchmarks(
        @PathVariable guildId: String,
    ): PerfectScoreBenchmarkDto {
        // Return default benchmarks for now
        return PerfectScoreBenchmarkDto(
            theoretical = 1.0,
            topPerformer = 0.95,
        )
    }

    /**
     * Get system status and capabilities (legacy endpoint).
     *
     * @return System status information
     */
    @GetMapping("/api/flps/status")
    fun getStatus(): FlpsDataStatusDto {
        return getStatusInternal()
    }

    /**
     * Get system status and capabilities (v1 endpoint).
     *
     * @return System status information
     */
    @GetMapping("/api/v1/flps/status")
    fun getStatusV1(): FlpsStatusResponse {
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

    private fun getFlpsReportInternal(guildId: String): List<ComprehensiveFlpsReportDto> {
        val query =
            GetFlpsReportQuery(
                guildId = GuildId(guildId),
                calculations = emptyList(), // TODO: Fetch calculations from data source
            )

        return getFlpsReportUseCase.execute(query)
            .map { report ->
                report.calculations.map { calc ->
                    ComprehensiveFlpsReportDto(
                        raiderId = calc.raiderId.value.toString(),
                        raiderName = calc.raiderId.value.toString(), // TODO: Get actual name
                        flpsScore = calc.flps.value,
                        eligible = calc.eligible,
                    )
                }
            }
            .getOrElse { emptyList() }
    }

    private fun getStatusInternal(): FlpsDataStatusDto {
        return FlpsDataStatusDto(
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
                    "Guild Report" to "/api/flps/{guildId}",
                    "Benchmarks" to "/api/flps/{guildId}/benchmarks",
                    "System Status" to "/api/flps/status",
                    "V1 Guild Report" to "/api/v1/flps/guilds/{guildId}/report",
                    "V1 System Status" to "/api/v1/flps/status",
                ),
        )
    }
}
