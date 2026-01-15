package com.edgerush.lootman.api.trial

import com.edgerush.lootman.domain.trial.model.Trial
import com.edgerush.lootman.domain.trial.model.TrialOutcome
import com.edgerush.lootman.domain.trial.model.TrialStatus
import java.time.Instant

/**
 * DTO for Trial entity.
 */
data class TrialDto(
    val id: String,
    val applicationId: String,
    val raiderId: Long?,
    val guildId: String,
    val status: TrialStatus,
    val startDate: Instant,
    val endDate: Instant?,
    val expectedEndDate: Instant,
    val raidsAttended: Int,
    val raidsRequired: Int,
    val attendanceRate: Double?,
    val averagePerformance: Double?,
    val deathsPerRaid: Double?,
    val progressPercentage: Double,
    val isComplete: Boolean,
    val outcome: TrialOutcome?,
    val outcomeReason: String?,
    val promotedBy: String?,
    val promotedAt: Instant?,
    val createdAt: Instant,
    val lastUpdated: Instant,
) {
    companion object {
        fun from(trial: Trial): TrialDto =
            TrialDto(
                id = trial.id.value,
                applicationId = trial.applicationId.value,
                raiderId = trial.raiderId,
                guildId = trial.guildId.value,
                status = trial.status,
                startDate = trial.startDate,
                endDate = trial.endDate,
                expectedEndDate = trial.expectedEndDate,
                raidsAttended = trial.raidsAttended,
                raidsRequired = trial.raidsRequired,
                attendanceRate = trial.attendanceRate,
                averagePerformance = trial.averagePerformance,
                deathsPerRaid = trial.deathsPerRaid,
                progressPercentage = trial.progressPercentage,
                isComplete = trial.isComplete,
                outcome = trial.outcome,
                outcomeReason = trial.outcomeReason,
                promotedBy = trial.promotedBy,
                promotedAt = trial.promotedAt,
                createdAt = trial.createdAt,
                lastUpdated = trial.lastUpdated,
            )
    }
}

/**
 * Request to create a new trial.
 */
data class CreateTrialRequest(
    val applicationId: String,
    val guildId: String,
    val raidsRequired: Int = 8,
    val raiderId: Long? = null,
)

/**
 * Request to update trial metrics.
 */
data class UpdateMetricsRequest(
    val raidsAttended: Int,
    val attendanceRate: Double,
    val averagePerformance: Double,
    val deathsPerRaid: Double,
)

/**
 * Request to promote a trial.
 */
data class PromoteTrialRequest(
    val promoterId: String,
    val reason: String,
)

/**
 * Request to extend a trial.
 */
data class ExtendTrialRequest(
    val extenderId: String,
    val additionalRaids: Int,
    val reason: String,
)

/**
 * Request to end a trial.
 */
data class EndTrialRequest(
    val officerId: String,
    val outcome: TrialOutcome,
    val reason: String,
)

/**
 * Paginated list of trials.
 */
data class TrialListResponse(
    val trials: List<TrialDto>,
    val total: Long,
    val offset: Long,
    val limit: Int,
)
