package com.edgerush.datasync.api

import com.edgerush.datasync.service.GuildManagementService
import com.edgerush.datasync.service.ComprehensiveFlpsReport
import com.edgerush.datasync.service.PerfectScoreBenchmark
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/flps")
class FlpsController(
    private val guildManagementService: GuildManagementService
) {

    /**
     * Single comprehensive endpoint per guild returning all FLPS data:
     * - Raw scores and percentages for all components
     * - Behavioral scoring and breakdown
     * - Loot ban status and information
     * - Eligibility determination with reasons
     */
    @GetMapping("/{guildId}")
    fun getComprehensiveFlpsReport(
        @PathVariable guildId: String
    ): List<ComprehensiveFlpsReport> {
        return guildManagementService.calculateComprehensiveFlpsReport(guildId)
    }

    /**
     * Get perfect score benchmarks for a guild (for debugging/admin purposes)
     */
    @GetMapping("/{guildId}/benchmarks")
    fun getPerfectScoreBenchmarks(
        @PathVariable guildId: String
    ): PerfectScoreBenchmark {
        return guildManagementService.calculatePerfectScoreBenchmarks(guildId)
    }

    /**
     * Status endpoint showing system capabilities
     */
    @GetMapping("/status")
    fun getDataStatus(): FlpsDataStatus {
        return FlpsDataStatus(
            message = "Comprehensive FLPS system with behavioral scoring and loot ban management",
            features = listOf(
                "Real WoWAudit data integration",
                "Guild-specific FLPS modifiers",
                "Perfect score benchmarking (theoretical/top performer/custom)",
                "Raw scores AND percentage breakdowns",
                "Behavioral scoring with guild leader management",
                "Time-limited loot bans",
                "Comprehensive eligibility determination",
                "Single endpoint per guild for all data"
            ),
            endpoints = mapOf(
                "Guild FLPS Report" to "/api/flps/{guildId}",
                "Guild Benchmarks" to "/api/flps/{guildId}/benchmarks",
                "System Status" to "/api/flps/status"
            )
        )
    }
}

data class FlpsDataStatus(
    val message: String,
    val features: List<String>,
    val endpoints: Map<String, String>
)