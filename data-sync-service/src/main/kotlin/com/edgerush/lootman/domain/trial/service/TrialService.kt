package com.edgerush.lootman.domain.trial.service

import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.trial.model.Trial
import com.edgerush.lootman.domain.trial.model.TrialId
import com.edgerush.lootman.domain.trial.model.TrialOutcome
import com.edgerush.lootman.domain.trial.model.TrialStatus
import com.edgerush.lootman.domain.trial.repository.TrialRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for managing trial periods.
 *
 * Provides business logic for creating, updating, and completing
 * trial periods for new guild members.
 */
@Service
@Transactional
class TrialService(
    private val trialRepository: TrialRepository,
) {
    /**
     * Creates a new trial for an approved application.
     *
     * @param applicationId The ID of the approved application
     * @param guildId The guild ID
     * @param raidsRequired Number of raids required for the trial
     * @param raiderId Optional raider ID if already linked
     * @throws IllegalStateException if a trial already exists for this application
     */
    fun createTrial(
        applicationId: ApplicationId,
        guildId: GuildId,
        raidsRequired: Int,
        raiderId: Long? = null,
    ): Trial {
        // Check if trial already exists
        val existingTrial = trialRepository.findByApplicationId(applicationId)
        if (existingTrial != null) {
            throw IllegalStateException("Trial already exists for application ${applicationId.value}")
        }

        val trial =
            Trial.create(
                applicationId = applicationId,
                guildId = guildId,
                raidsRequired = raidsRequired,
                raiderId = raiderId,
            )

        return trialRepository.save(trial)
    }

    /**
     * Gets a trial by its ID.
     */
    @Transactional(readOnly = true)
    fun getTrial(trialId: TrialId): Trial? {
        return trialRepository.findById(trialId)
    }

    /**
     * Gets a trial by application ID.
     */
    @Transactional(readOnly = true)
    fun getTrialByApplicationId(applicationId: ApplicationId): Trial? {
        return trialRepository.findByApplicationId(applicationId)
    }

    /**
     * Gets all active (not terminated) trials for a guild.
     */
    @Transactional(readOnly = true)
    fun getActiveTrials(guildId: GuildId): List<Trial> {
        return trialRepository.findActiveTrialsByGuildId(guildId)
    }

    /**
     * Lists all trials for a guild with pagination.
     */
    @Transactional(readOnly = true)
    fun listTrials(
        guildId: GuildId,
        offset: Long = 0,
        limit: Int = 50,
    ): List<Trial> {
        return trialRepository.findByGuildId(guildId, offset, limit)
    }

    /**
     * Lists trials for a guild filtered by status.
     */
    @Transactional(readOnly = true)
    fun listTrialsByStatus(
        guildId: GuildId,
        status: TrialStatus,
        offset: Long = 0,
        limit: Int = 50,
    ): List<Trial> {
        return trialRepository.findByGuildIdAndStatus(guildId, status, offset, limit)
    }

    /**
     * Counts all trials for a guild.
     */
    @Transactional(readOnly = true)
    fun countTrials(guildId: GuildId): Long {
        return trialRepository.countByGuildId(guildId)
    }

    /**
     * Counts trials for a guild filtered by status.
     */
    @Transactional(readOnly = true)
    fun countTrialsByStatus(
        guildId: GuildId,
        status: TrialStatus,
    ): Long {
        return trialRepository.countByGuildIdAndStatus(guildId, status)
    }

    /**
     * Updates trial metrics based on raid participation.
     *
     * @throws IllegalArgumentException if trial is not found
     */
    fun updateMetrics(
        trialId: TrialId,
        raidsAttended: Int,
        attendanceRate: Double,
        averagePerformance: Double,
        deathsPerRaid: Double,
    ): Trial {
        val trial = findTrialOrThrow(trialId)

        val updated =
            trial.updateMetrics(
                raidsAttended = raidsAttended,
                attendanceRate = attendanceRate,
                averagePerformance = averagePerformance,
                deathsPerRaid = deathsPerRaid,
            )

        return trialRepository.save(updated)
    }

    /**
     * Promotes a trial raider to full member.
     *
     * @throws IllegalArgumentException if trial is not found
     * @throws IllegalStateException if trial cannot be promoted
     */
    fun promoteTrial(
        trialId: TrialId,
        promoterId: String,
        reason: String,
    ): Trial {
        val trial = findTrialOrThrow(trialId)
        val promoted = trial.promote(promoterId, reason)
        return trialRepository.save(promoted)
    }

    /**
     * Extends a trial period for additional evaluation.
     *
     * @throws IllegalArgumentException if trial is not found
     * @throws IllegalStateException if trial cannot be extended
     */
    fun extendTrial(
        trialId: TrialId,
        extenderId: String,
        additionalRaids: Int,
        reason: String,
    ): Trial {
        val trial = findTrialOrThrow(trialId)
        val extended = trial.extend(extenderId, additionalRaids, reason)
        return trialRepository.save(extended)
    }

    /**
     * Ends a trial with a non-promotion outcome.
     *
     * @throws IllegalArgumentException if trial is not found
     * @throws IllegalStateException if trial cannot be ended
     */
    fun endTrial(
        trialId: TrialId,
        officerId: String,
        outcome: TrialOutcome,
        reason: String,
    ): Trial {
        val trial = findTrialOrThrow(trialId)
        val ended = trial.endTrial(officerId, outcome, reason)
        return trialRepository.save(ended)
    }

    /**
     * Deletes a trial by its ID.
     */
    fun deleteTrial(trialId: TrialId) {
        trialRepository.deleteById(trialId)
    }

    private fun findTrialOrThrow(trialId: TrialId): Trial {
        return trialRepository.findById(trialId)
            ?: throw IllegalArgumentException("Trial not found: ${trialId.value}")
    }
}
