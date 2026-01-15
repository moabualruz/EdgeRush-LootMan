package com.edgerush.lootman.domain.raidplan.repository

import com.edgerush.lootman.domain.raidplan.model.RaidPlan
import com.edgerush.lootman.domain.shared.GuildId

/**
 * Repository interface for RaidPlan aggregate.
 */
interface RaidPlanRepository {
    /**
     * Finds a raid plan by its ID.
     */
    fun findById(id: String): RaidPlan?

    /**
     * Finds a raid plan by its share token.
     */
    fun findByShareToken(shareToken: String): RaidPlan?

    /**
     * Finds all raid plans for a guild.
     */
    fun findByGuildId(guildId: GuildId): List<RaidPlan>

    /**
     * Finds all raid plans for a guild with pagination.
     */
    fun findByGuildId(
        guildId: GuildId,
        offset: Long,
        limit: Int,
    ): List<RaidPlan>

    /**
     * Counts raid plans for a guild.
     */
    fun countByGuildId(guildId: GuildId): Long

    /**
     * Finds raid plans for a specific encounter within a guild.
     */
    fun findByGuildIdAndEncounterId(
        guildId: GuildId,
        encounterId: Int,
    ): List<RaidPlan>

    /**
     * Finds raid plans created by a specific user.
     */
    fun findByCreatedBy(userId: Long): List<RaidPlan>

    /**
     * Saves a raid plan (creates or updates).
     */
    fun save(raidPlan: RaidPlan): RaidPlan

    /**
     * Deletes a raid plan by its ID.
     */
    fun delete(id: String)

    /**
     * Checks if a raid plan exists.
     */
    fun existsById(id: String): Boolean
}
