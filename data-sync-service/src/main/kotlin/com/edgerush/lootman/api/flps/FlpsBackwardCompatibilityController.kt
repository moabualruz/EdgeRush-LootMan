package com.edgerush.lootman.api.flps

import com.edgerush.datasync.service.ComprehensiveFlpsReport
import com.edgerush.datasync.service.GuildManagementService
import com.edgerush.datasync.service.PerfectScoreBenchmark
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/flps")
class FlpsBackwardCompatibilityController(
    private val guildManagementService: GuildManagementService,
) {
    @GetMapping("/{guildId}")
    fun getComprehensiveFlpsReport(
        @PathVariable guildId: String,
    ): List<ComprehensiveFlpsReport> {
        return guildManagementService.calculateComprehensiveFlpsReport(guildId)
    }

    @GetMapping("/{guildId}/benchmarks")
    fun getPerfectScoreBenchmarks(
        @PathVariable guildId: String,
    ): PerfectScoreBenchmark {
        return guildManagementService.calculatePerfectScoreBenchmarks(guildId)
    }

    @GetMapping("/status")
    fun getDataStatus(): FlpsDataStatus {
        return FlpsDataStatus(
            message = "Comprehensive FLPS system with behavioral scoring and loot ban management (Legacy API)",
            features =
                listOf(
                    "Real WoWAudit data integration",
                    "Guild-specific FLPS modifiers",
                    "Perfect score benchmarking",
                    "Raw scores AND percentage breakdowns",
                    "Behavioral scoring with guild leader management",
                    "Time-limited loot bans",
                    "Comprehensive eligibility determination",
                    "Single endpoint per guild for all data",
                ),
            endpoints =
                mapOf(
                    "Guild FLPS Report (Legacy)" to "/api/flps/{guildId}",
                    "Guild Benchmarks (Legacy)" to "/api/flps/{guildId}/benchmarks",
                    "System Status (Legacy)" to "/api/flps/status",
                    "New API Base" to "/api/v1/flps/",
                ),
        )
    }
}

data class FlpsDataStatus(
    val message: String,
    val features: List<String>,
    val endpoints: Map<String, String>,
)
