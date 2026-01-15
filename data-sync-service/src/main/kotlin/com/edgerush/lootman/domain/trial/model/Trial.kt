package com.edgerush.lootman.domain.trial.model

import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.shared.GuildId
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Entity representing a trial period for a new guild member.
 *
 * Trials are created when an application is approved. During the trial period,
 * the raider's attendance, performance, and deaths are tracked to evaluate
 * their fit with the guild before full membership.
 */
@ConsistentCopyVisibility
data class Trial private constructor(
    val id: TrialId,
    val applicationId: ApplicationId,
    val raiderId: Long?,
    val guildId: GuildId,
    // Status
    val status: TrialStatus,
    val startDate: Instant,
    val endDate: Instant?,
    val expectedEndDate: Instant,
    // Metrics
    val raidsAttended: Int,
    val raidsRequired: Int,
    val attendanceRate: Double?,
    val averagePerformance: Double?,
    val deathsPerRaid: Double?,
    // Outcome
    val outcome: TrialOutcome?,
    val outcomeReason: String?,
    val promotedBy: String?,
    val promotedAt: Instant?,
    // Timestamps
    val createdAt: Instant,
    val lastUpdated: Instant,
) {
    /**
     * Progress percentage towards completing the trial.
     * Capped at 100%.
     */
    val progressPercentage: Double
        get() = minOf(100.0, (raidsAttended.toDouble() / raidsRequired.toDouble()) * 100.0)

    /**
     * Whether the trial has met the required number of raids.
     */
    val isComplete: Boolean
        get() = raidsAttended >= raidsRequired

    companion object {
        private const val DAYS_PER_RAID = 3.5 // ~2 raids per week

        /**
         * Reconstructs a Trial from persisted data.
         */
        fun reconstruct(
            id: TrialId,
            applicationId: ApplicationId,
            raiderId: Long?,
            guildId: GuildId,
            status: TrialStatus,
            startDate: Instant,
            endDate: Instant?,
            expectedEndDate: Instant,
            raidsAttended: Int,
            raidsRequired: Int,
            attendanceRate: Double?,
            averagePerformance: Double?,
            deathsPerRaid: Double?,
            outcome: TrialOutcome?,
            outcomeReason: String?,
            promotedBy: String?,
            promotedAt: Instant?,
            createdAt: Instant,
            lastUpdated: Instant,
        ): Trial =
            Trial(
                id = id,
                applicationId = applicationId,
                raiderId = raiderId,
                guildId = guildId,
                status = status,
                startDate = startDate,
                endDate = endDate,
                expectedEndDate = expectedEndDate,
                raidsAttended = raidsAttended,
                raidsRequired = raidsRequired,
                attendanceRate = attendanceRate,
                averagePerformance = averagePerformance,
                deathsPerRaid = deathsPerRaid,
                outcome = outcome,
                outcomeReason = outcomeReason,
                promotedBy = promotedBy,
                promotedAt = promotedAt,
                createdAt = createdAt,
                lastUpdated = lastUpdated,
            )

        /**
         * Creates a new Trial with ACTIVE status.
         */
        fun create(
            applicationId: ApplicationId,
            guildId: GuildId,
            raidsRequired: Int,
            raiderId: Long? = null,
        ): Trial {
            require(raidsRequired > 0) { "Raids required must be positive" }

            val now = Instant.now()
            val expectedDays = (raidsRequired * DAYS_PER_RAID).toLong()

            return Trial(
                id = TrialId.generate(),
                applicationId = applicationId,
                raiderId = raiderId,
                guildId = guildId,
                status = TrialStatus.ACTIVE,
                startDate = now,
                endDate = null,
                expectedEndDate = now.plus(expectedDays, ChronoUnit.DAYS),
                raidsAttended = 0,
                raidsRequired = raidsRequired,
                attendanceRate = null,
                averagePerformance = null,
                deathsPerRaid = null,
                outcome = null,
                outcomeReason = null,
                promotedBy = null,
                promotedAt = null,
                createdAt = now,
                lastUpdated = now,
            )
        }
    }

    /**
     * Updates trial metrics based on recent raid participation.
     */
    fun updateMetrics(
        raidsAttended: Int,
        attendanceRate: Double,
        averagePerformance: Double,
        deathsPerRaid: Double,
    ): Trial {
        check(!status.isTerminal) { "Cannot update metrics on a completed trial" }
        require(attendanceRate in 0.0..1.0) { "Attendance rate must be between 0 and 1" }
        require(averagePerformance in 0.0..100.0) { "Average performance must be between 0 and 100" }
        require(deathsPerRaid >= 0.0) { "Deaths per raid cannot be negative" }

        return copy(
            raidsAttended = raidsAttended,
            attendanceRate = attendanceRate,
            averagePerformance = averagePerformance,
            deathsPerRaid = deathsPerRaid,
            lastUpdated = Instant.now(),
        )
    }

    /**
     * Promotes the trial raider to full member.
     */
    fun promote(
        promoterId: String,
        reason: String,
    ): Trial {
        check(status == TrialStatus.ACTIVE || status == TrialStatus.EXTENDED) {
            "Can only promote active or extended trials"
        }

        val now = Instant.now()
        return copy(
            status = TrialStatus.PROMOTED,
            endDate = now,
            outcome = TrialOutcome.PROMOTED,
            outcomeReason = reason,
            promotedBy = promoterId,
            promotedAt = now,
            lastUpdated = now,
        )
    }

    /**
     * Extends the trial period for additional evaluation.
     */
    fun extend(
        extenderId: String,
        additionalRaids: Int,
        reason: String,
    ): Trial {
        check(status == TrialStatus.ACTIVE || status == TrialStatus.EXTENDED) {
            "Can only extend active or already extended trials"
        }
        require(additionalRaids > 0) { "Additional raids must be positive" }

        val newRaidsRequired = raidsRequired + additionalRaids
        val additionalDays = (additionalRaids * DAYS_PER_RAID).toLong()

        return copy(
            status = TrialStatus.EXTENDED,
            raidsRequired = newRaidsRequired,
            expectedEndDate = expectedEndDate.plus(additionalDays, ChronoUnit.DAYS),
            outcomeReason = reason,
            lastUpdated = Instant.now(),
        )
    }

    /**
     * Ends the trial with a non-promotion outcome.
     */
    fun endTrial(
        officerId: String,
        outcome: TrialOutcome,
        reason: String,
    ): Trial {
        check(status == TrialStatus.ACTIVE || status == TrialStatus.EXTENDED) {
            "Can only end active or extended trials"
        }
        require(outcome != TrialOutcome.PROMOTED) {
            "Use promote() method for PROMOTED outcome"
        }

        val now = Instant.now()
        return copy(
            status = TrialStatus.ENDED,
            endDate = now,
            outcome = outcome,
            outcomeReason = reason,
            lastUpdated = now,
        )
    }
}
