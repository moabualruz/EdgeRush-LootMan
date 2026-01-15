package com.edgerush.lootman.infrastructure.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Custom application metrics for monitoring FLPS calculations, loot awards,
 * attendance tracking, and audit operations.
 */
@Component
class CustomMetrics(private val meterRegistry: MeterRegistry) {
    /**
     * Record a FLPS calculation event.
     */
    fun recordFlpsCalculation(
        guildId: String,
        score: Double,
    ) {
        Counter.builder("flps.calculations.total")
            .description("Total number of FLPS calculations performed")
            .tag("guild_id", guildId)
            .register(meterRegistry)
            .increment()

        meterRegistry.summary("flps.score", "guild_id", guildId)
            .record(score)
    }

    /**
     * Record FLPS calculation duration in milliseconds.
     */
    fun recordFlpsCalculationDuration(durationMs: Long) {
        Timer.builder("flps.calculation.duration")
            .description("Duration of FLPS calculations")
            .register(meterRegistry)
            .record(Duration.ofMillis(durationMs))
    }

    /**
     * Record a loot award event.
     */
    fun recordLootAward(
        guildId: String,
        tier: String,
    ) {
        Counter.builder("loot.awards.total")
            .description("Total number of loot awards")
            .tag("guild_id", guildId)
            .tag("tier", tier)
            .register(meterRegistry)
            .increment()
    }

    /**
     * Record a loot revoke event.
     */
    fun recordLootRevoke(guildId: String) {
        Counter.builder("loot.revokes.total")
            .description("Total number of loot revokes")
            .tag("guild_id", guildId)
            .register(meterRegistry)
            .increment()
    }

    /**
     * Record attendance tracking event.
     */
    fun recordAttendanceTracked(
        guildId: String,
        raiderCount: Int,
    ) {
        Counter.builder("attendance.tracked.total")
            .description("Total number of attendance tracking events")
            .tag("guild_id", guildId)
            .register(meterRegistry)
            .increment()

        meterRegistry.summary("attendance.raiders.count", "guild_id", guildId)
            .record(raiderCount.toDouble())
    }

    /**
     * Record audit log entry.
     */
    fun recordAuditEntry(
        operation: String,
        entityType: String,
    ) {
        Counter.builder("audit.entries.total")
            .description("Total number of audit log entries")
            .tag("operation", operation)
            .tag("entity_type", entityType)
            .register(meterRegistry)
            .increment()
    }
}
