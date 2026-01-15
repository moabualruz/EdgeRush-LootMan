package com.edgerush.lootman.api.flps

import com.edgerush.datasync.config.CacheConfig
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.application.flps.CalculateFlpsScoreCommand
import com.edgerush.lootman.application.flps.CalculateFlpsScoreUseCase
import com.edgerush.lootman.application.flps.FlpsCalculationResult
import com.edgerush.lootman.application.flps.FlpsComponentCalculator
import com.edgerush.lootman.application.flps.FlpsDataAssemblerService
import com.edgerush.lootman.application.flps.GetFlpsReportQuery
import com.edgerush.lootman.application.flps.GetFlpsReportUseCase
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    private val configPreviewService: FlpsConfigPreviewService,
    private val currentUserService: CurrentUserService,
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
     * Get FLPS score for the current authenticated user.
     *
     * Returns the FLPS score and breakdown for the user's primary linked character.
     *
     * @param guildId The guild identifier
     * @param user The authenticated user (injected from security context)
     * @return Personal FLPS score response
     */
    @GetMapping("/api/v1/flps/guilds/{guildId}/me")
    @Operation(
        summary = "Get my FLPS score",
        description = "Returns the FLPS score for the current user's primary linked character",
    )
    fun getMyFlpsScore(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): PersonalFlpsResponse {
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(user)
        return calculateFlpsForRaider(guildId, raiderId)
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
     * Get FLPS leaderboard for a guild with optional filters.
     *
     * Returns a filtered and paginated leaderboard of raiders sorted by FLPS score.
     * Supports filtering by role, class, and eligibility status.
     *
     * @param guildId The guild identifier
     * @param role Filter by role (dps, healer, tank)
     * @param characterClass Filter by character class
     * @param eligible Filter by eligibility status
     * @param limit Maximum number of results (default 10, max 100)
     * @param offset Number of results to skip (for pagination)
     * @return Filtered leaderboard response
     */
    @GetMapping("/api/v1/flps/guilds/{guildId}/leaderboard")
    @Operation(
        summary = "Get FLPS leaderboard",
        description = "Returns a filtered and paginated leaderboard of raiders sorted by FLPS score",
    )
    @Cacheable(
        value = [CacheConfig.FLPS_LEADERBOARD],
        key = "#guildId + '-' + #role + '-' + #characterClass + '-' + #eligible + '-' + #limit + '-' + #offset",
    )
    fun getLeaderboard(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Filter by role (dps, healer, tank)")
        @RequestParam(required = false) role: String?,
        @Parameter(description = "Filter by character class (warrior, mage, etc.)")
        @RequestParam(required = false, name = "class") characterClass: String?,
        @Parameter(description = "Filter by eligibility status")
        @RequestParam(required = false) eligible: Boolean?,
        @Parameter(description = "Maximum number of results (default 10, max 100)")
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @Parameter(description = "Number of results to skip")
        @RequestParam(required = false, defaultValue = "0") offset: Int,
    ): LeaderboardResponse {
        val effectiveLimit = limit.coerceIn(1, 100)

        // Fetch all raider data from database
        val raiderDataList = flpsDataAssembler.assembleFlpsData(GuildId(guildId))
        val exampleItemId = com.edgerush.lootman.domain.shared.ItemId(12345L)

        // Calculate FLPS for each raider and create leaderboard entries
        val allEntries =
            raiderDataList.map { raiderData ->
                val acs = componentCalculator.calculateACS(raiderData.attendance)
                val mas = componentCalculator.calculateMAS()
                val eps = componentCalculator.calculateEPS(raiderData.gear)
                val uv = componentCalculator.calculateUV(raiderData.wishlist, exampleItemId)
                val tb = componentCalculator.calculateTierBonus(raiderData.gear)
                val rm = componentCalculator.calculateRoleMultiplier(raiderData.raider.role)
                val rdf = componentCalculator.calculateRDF(raiderData.lootHistory, raiderData.activeBans)

                val command =
                    CalculateFlpsScoreCommand(
                        guildId = GuildId(guildId),
                        raiderId = raiderData.raider.id,
                        itemId = exampleItemId,
                        acs = acs, mas = mas, eps = eps, uv = uv, tb = tb, rm = rm, rdf = rdf,
                    )

                val result = calculateFlpsScoreUseCase.execute(command).getOrThrow()

                LeaderboardEntry(
                    rank = 0, // Will be set after filtering/sorting
                    raiderId = raiderData.raider.id.value,
                    raiderName = raiderData.raider.characterName,
                    characterClass = raiderData.raider.characterClass.name,
                    role = raiderData.raider.role.name,
                    flpsScore = result.flps.value,
                    eligible = result.eligible,
                )
            }

        // Apply filters
        var filtered = allEntries

        role?.let { r ->
            filtered = filtered.filter { it.role.equals(r, ignoreCase = true) }
        }

        characterClass?.let { c ->
            filtered = filtered.filter { it.characterClass.equals(c, ignoreCase = true) }
        }

        eligible?.let { e ->
            filtered = filtered.filter { it.eligible == e }
        }

        // Sort by score descending and assign ranks
        val sorted =
            filtered
                .sortedByDescending { it.flpsScore }
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }

        // Apply pagination
        val total = sorted.size
        val paginated = sorted.drop(offset).take(effectiveLimit)

        return LeaderboardResponse(
            guildId = guildId,
            entries = paginated,
            total = total,
            limit = effectiveLimit,
            offset = offset,
            filters =
                LeaderboardFilters(
                    role = role,
                    characterClass = characterClass,
                    eligible = eligible,
                ),
        )
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
     * Get current FLPS configuration for a guild.
     *
     * @param guildId The guild identifier
     * @return Current configuration summary
     */
    @GetMapping("/api/v1/flps/guilds/{guildId}/config")
    @Operation(
        summary = "Get current FLPS configuration",
        description = "Returns the current FLPS configuration settings for a guild",
    )
    fun getCurrentConfig(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
    ): FlpsConfigSummary {
        return configPreviewService.getCurrentConfig(guildId)
    }

    /**
     * Preview the impact of FLPS configuration changes.
     *
     * This endpoint allows guild administrators to see how configuration changes
     * would affect raiders' FLPS scores before applying them. It returns a
     * detailed impact analysis including:
     * - Current vs proposed configuration
     * - Per-raider score changes
     * - Eligibility changes
     * - Ranking changes
     *
     * @param guildId The guild identifier
     * @param request The proposed configuration changes
     * @return Preview response with impact analysis
     */
    @PostMapping("/api/v1/flps/guilds/{guildId}/config/preview")
    @Operation(
        summary = "Preview configuration changes",
        description = "Shows how proposed configuration changes would affect FLPS scores without applying them",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Preview generated successfully"),
        ApiResponse(responseCode = "400", description = "Invalid configuration values"),
    )
    fun previewConfigChanges(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @RequestBody request: ConfigPreviewRequest,
    ): ConfigPreviewResponse {
        return configPreviewService.previewConfigChanges(guildId, request)
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
            val command =
                CalculateFlpsScoreCommand(
                    guildId = GuildId(guildId),
                    raiderId = raiderData.raider.id,
                    itemId = exampleItemId,
                    acs = componentCalculator.calculateACS(raiderData.attendance),
                    mas = componentCalculator.calculateMAS(),
                    eps = componentCalculator.calculateEPS(raiderData.gear),
                    uv = componentCalculator.calculateUV(raiderData.wishlist, exampleItemId),
                    tb = componentCalculator.calculateTierBonus(raiderData.gear),
                    rm = componentCalculator.calculateRoleMultiplier(raiderData.raider.role),
                    rdf = componentCalculator.calculateRDF(raiderData.lootHistory, raiderData.activeBans),
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

            val command =
                CalculateFlpsScoreCommand(
                    guildId = GuildId(guildId),
                    raiderId = raiderData.raider.id,
                    itemId = exampleItemId,
                    acs = acs,
                    mas = mas,
                    eps = eps,
                    uv = uv,
                    tb = tb,
                    rm = rm,
                    rdf = rdf,
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

    /**
     * Calculate FLPS for a specific raider and return personal response.
     */
    private fun calculateFlpsForRaider(
        guildId: String,
        raiderId: RaiderId,
    ): PersonalFlpsResponse {
        // Fetch all raider data from database
        val raiderDataList = flpsDataAssembler.assembleFlpsData(GuildId(guildId))
        val exampleItemId = com.edgerush.lootman.domain.shared.ItemId(12345L)

        // Find the specific raider
        val raiderData =
            raiderDataList.find { it.raider.id == raiderId }
                ?: throw IllegalArgumentException("Raider not found in guild: ${raiderId.value}")

        // Calculate components
        val acs = componentCalculator.calculateACS(raiderData.attendance)
        val mas = componentCalculator.calculateMAS()
        val eps = componentCalculator.calculateEPS(raiderData.gear)
        val uv = componentCalculator.calculateUV(raiderData.wishlist, exampleItemId)
        val tb = componentCalculator.calculateTierBonus(raiderData.gear)
        val rm = componentCalculator.calculateRoleMultiplier(raiderData.raider.role)
        val rdf = componentCalculator.calculateRDF(raiderData.lootHistory, raiderData.activeBans)

        val command =
            CalculateFlpsScoreCommand(
                guildId = GuildId(guildId),
                raiderId = raiderId,
                itemId = exampleItemId,
                acs = acs, mas = mas, eps = eps, uv = uv, tb = tb, rm = rm, rdf = rdf,
            )

        val result = calculateFlpsScoreUseCase.execute(command).getOrThrow()

        // Calculate all scores to determine rank
        val allScores =
            raiderDataList.map { rd ->
                val rdAcs = componentCalculator.calculateACS(rd.attendance)
                val rdMas = componentCalculator.calculateMAS()
                val rdEps = componentCalculator.calculateEPS(rd.gear)
                val rdUv = componentCalculator.calculateUV(rd.wishlist, exampleItemId)
                val rdTb = componentCalculator.calculateTierBonus(rd.gear)
                val rdRm = componentCalculator.calculateRoleMultiplier(rd.raider.role)
                val rdRdf = componentCalculator.calculateRDF(rd.lootHistory, rd.activeBans)

                val rdCommand =
                    CalculateFlpsScoreCommand(
                        guildId = GuildId(guildId),
                        raiderId = rd.raider.id,
                        itemId = exampleItemId,
                        acs = rdAcs, mas = rdMas, eps = rdEps, uv = rdUv, tb = rdTb, rm = rdRm, rdf = rdRdf,
                    )

                rd.raider.id to calculateFlpsScoreUseCase.execute(rdCommand).getOrThrow().flps.value
            }.sortedByDescending { it.second }

        val rank = allScores.indexOfFirst { it.first == raiderId } + 1

        return PersonalFlpsResponse(
            raiderId = raiderId.value,
            raiderName = raiderData.raider.characterName,
            characterClass = raiderData.raider.characterClass.name,
            role = raiderData.raider.role.name,
            flpsScore = result.flps.value,
            rank = rank,
            totalRaiders = allScores.size,
            eligible = result.eligible,
            breakdown =
                FlpsBreakdownResponse(
                    rms =
                        RmsBreakdownResponse(
                            value = result.rms.value,
                            acs = acs.value,
                            mas = mas.value,
                            eps = eps.value,
                        ),
                    ipi =
                        IpiBreakdownResponse(
                            value = result.ipi.value,
                            uv = uv.value,
                            tierBonus = tb.value,
                            roleMultiplier = rm.value,
                        ),
                    rdf = rdf.value,
                ),
        )
    }
}
