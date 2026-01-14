package com.edgerush.lootman.api.performance

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.domain.flps.repository.RaiderPerformanceRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/v1/performance")
@Tag(name = "Performance", description = "Performance metrics endpoints")
class PerformanceController(
    private val currentUserService: CurrentUserService,
    private val raiderPerformanceRepository: RaiderPerformanceRepository,
    private val raiderEntityRepository: RaiderEntityRepository,
) {
    private val isoFormatter = DateTimeFormatter.ISO_INSTANT

    @GetMapping("/guilds/{guildId}/me")
    @Operation(summary = "Get performance metrics for the current user")
    fun getMyPerformance(
        @PathVariable guildId: String,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): PerformanceMetricsResponse {
        currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId))
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser)

        val raider = raiderEntityRepository.findById(raiderId.value)
            ?: throw IllegalArgumentException("Raider not found: ${raiderId.value}")

        return buildPerformanceResponse(raiderId, raider.characterName, GuildId(guildId))
    }

    @GetMapping("/guilds/{guildId}/raiders/{raiderId}")
    @Operation(summary = "Get performance metrics for a specific raider")
    fun getPerformance(
        @PathVariable guildId: String,
        @PathVariable raiderId: Long,
    ): PerformanceMetricsResponse {
        val raider = raiderEntityRepository.findById(raiderId)
            ?: throw IllegalArgumentException("Raider not found: $raiderId")

        if (raider.guildId != guildId) {
            throw IllegalArgumentException("Raider $raiderId does not belong to guild $guildId")
        }

        return buildPerformanceResponse(RaiderId(raiderId), raider.characterName, GuildId(guildId))
    }

    private fun buildPerformanceResponse(
        raiderId: RaiderId,
        characterName: String,
        guildId: GuildId,
    ): PerformanceMetricsResponse {
        val now = Instant.now()
        val oneWeekAgo = now.minus(7, ChronoUnit.DAYS)

        val performanceData = raiderPerformanceRepository.findByRaiderAndPeriod(
            raiderId,
            guildId,
            oneWeekAgo,
            now,
        )

        val dpa = performanceData?.deathsPerAttempt ?: 0.0
        val adt = performanceData?.avoidableDamagePercentage ?: 0.0

        // For now, return empty trend data as we don't have historical tracking yet
        // This would require a separate historical performance table
        val performanceTrend = emptyList<PerformanceDataPoint>()

        return PerformanceMetricsResponse(
            raiderId = raiderId.value,
            characterName = characterName,
            dpa = dpa,
            adt = adt,
            specAverage = 0.0, // Placeholder - would require spec-specific data
            performanceTrend = performanceTrend,
            lastUpdated = (performanceData?.periodEnd ?: now).atOffset(ZoneOffset.UTC).format(isoFormatter),
        )
    }
}
