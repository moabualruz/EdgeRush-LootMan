package com.edgerush.lootman.api.flps

import com.edgerush.lootman.application.flps.CalculateFlpsScoreCommand
import com.edgerush.lootman.application.flps.CalculateFlpsScoreUseCase
import com.edgerush.lootman.application.flps.FlpsCalculationResult
import com.edgerush.lootman.application.flps.FlpsComponentCalculator
import com.edgerush.lootman.application.flps.FlpsDataAssemblerService
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
    private val flpsDataAssembler: FlpsDataAssemblerService,
    private val componentCalculator: FlpsComponentCalculator,
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
        val calculations = calculateFlpsForAllRaiders(guildId)

        val query =
            GetFlpsReportQuery(
                guildId = GuildId(guildId),
                calculations = calculations,
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

    /**
     * Calculate FLPS for all raiders in a guild.
     *
     * @param guildId The guild identifier
     * @return List of FLPS calculation results for all raiders
     */
    private fun calculateFlpsForAllRaiders(guildId: String): List<FlpsCalculationResult> {
        // Fetch all raider data from database
        val raiderDataList = flpsDataAssembler.assembleFlpsData(GuildId(guildId))

        // For demonstration, calculate FLPS for a hypothetical item
        // In production, this would be for specific contested items
        val exampleItemId = com.edgerush.lootman.domain.shared.ItemId(12345L)

        // Calculate FLPS for each raider
        return raiderDataList.map { raiderData ->
            val command = CalculateFlpsScoreCommand(
                guildId = GuildId(guildId),
                raiderId = raiderData.raider.id,
                itemId = exampleItemId,
                acs = componentCalculator.calculateACS(raiderData.attendance),
                mas = componentCalculator.calculateMAS(),
                eps = componentCalculator.calculateEPS(raiderData.gear),
                uv = componentCalculator.calculateUV(raiderData.wishlist, exampleItemId),
                tb = componentCalculator.calculateTierBonus(raiderData.gear),
                rm = componentCalculator.calculateRoleMultiplier(raiderData.raider.role),
                rdf = componentCalculator.calculateRDF(raiderData.lootHistory, raiderData.activeBans)
            )

            calculateFlpsScoreUseCase.execute(command).getOrThrow()
        }
    }

    private fun getFlpsReportInternal(guildId: String): List<ComprehensiveFlpsReportDto> {
        // Fetch all raider data from database
        val raiderDataList = flpsDataAssembler.assembleFlpsData(GuildId(guildId))

        // For demonstration, calculate FLPS for a hypothetical item
        // In production, this would be for specific contested items
        val exampleItemId = com.edgerush.lootman.domain.shared.ItemId(12345L)

        // Calculate FLPS for each raider
        return raiderDataList.map { raiderData ->
            val acs = componentCalculator.calculateACS(raiderData.attendance)
            val mas = componentCalculator.calculateMAS()
            val eps = componentCalculator.calculateEPS(raiderData.gear)
            val uv = componentCalculator.calculateUV(raiderData.wishlist, exampleItemId)
            val tb = componentCalculator.calculateTierBonus(raiderData.gear)
            val rm = componentCalculator.calculateRoleMultiplier(raiderData.raider.role)
            val rdf = componentCalculator.calculateRDF(raiderData.lootHistory, raiderData.activeBans)

            val command = CalculateFlpsScoreCommand(
                guildId = GuildId(guildId),
                raiderId = raiderData.raider.id,
                itemId = exampleItemId,
                acs = acs,
                mas = mas,
                eps = eps,
                uv = uv,
                tb = tb,
                rm = rm,
                rdf = rdf
            )

            val result = calculateFlpsScoreUseCase.execute(command).getOrThrow()

            ComprehensiveFlpsReportDto(
                raiderId = raiderData.raider.id.value.toString(),
                raiderName = raiderData.raider.characterName,
                flpsScore = result.flps.value,
                eligible = result.eligible,
            )
        }.sortedByDescending { it.flpsScore }
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
