package com.edgerush.lootman.domain.trial.repository

import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.trial.model.Trial
import com.edgerush.lootman.domain.trial.model.TrialId
import com.edgerush.lootman.domain.trial.model.TrialStatus

/**
 * Repository interface for Trial entities.
 *
 * Provides persistence operations for managing trial periods
 * of new guild members.
 */
interface TrialRepository {
    /**
     * Saves a new or updated trial.
     */
    fun save(trial: Trial): Trial

    /**
     * Finds a trial by its ID.
     */
    fun findById(id: TrialId): Trial?

    /**
     * Finds a trial by application ID.
     */
    fun findByApplicationId(applicationId: ApplicationId): Trial?

    /**
     * Finds all trials for a guild.
     */
    fun findByGuildId(
        guildId: GuildId,
        offset: Long = 0,
        limit: Int = 50,
    ): List<Trial>

    /**
     * Finds trials for a guild filtered by status.
     */
    fun findByGuildIdAndStatus(
        guildId: GuildId,
        status: TrialStatus,
        offset: Long = 0,
        limit: Int = 50,
    ): List<Trial>

    /**
     * Finds all active and extended trials for a guild.
     */
    fun findActiveTrialsByGuildId(guildId: GuildId): List<Trial>

    /**
     * Finds a trial by raider ID.
     */
    fun findByRaiderId(raiderId: Long): Trial?

    /**
     * Counts trials for a guild.
     */
    fun countByGuildId(guildId: GuildId): Long

    /**
     * Counts trials for a guild filtered by status.
     */
    fun countByGuildIdAndStatus(
        guildId: GuildId,
        status: TrialStatus,
    ): Long

    /**
     * Deletes a trial by its ID.
     */
    fun deleteById(id: TrialId)

    /**
     * Checks if a trial exists with the given ID.
     */
    fun existsById(id: TrialId): Boolean
}
