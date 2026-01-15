package com.edgerush.lootman.api.behavioral

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for BehavioralAction operations.
 *
 * Extends the generic CrudService with behavioral-action-specific query methods.
 */
interface BehavioralActionCrudService : CrudService<Long, CreateBehavioralActionRequest, UpdateBehavioralActionRequest, BehavioralActionResponse> {
    /**
     * Find behavioral actions by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of behavioral actions for the guild
     */
    fun findByGuild(
        guildId: String,
        pageRequest: PageRequest,
    ): PagedResponse<BehavioralActionResponse>

    /**
     * Find active behavioral actions by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of active behavioral actions for the guild
     */
    fun findActiveByGuild(
        guildId: String,
        pageRequest: PageRequest,
    ): PagedResponse<BehavioralActionResponse>

    /**
     * Find behavioral actions by character with pagination.
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @param pageRequest Pagination parameters
     * @return Paginated list of behavioral actions for the character
     */
    fun findByCharacter(
        guildId: String,
        characterName: String,
        pageRequest: PageRequest,
    ): PagedResponse<BehavioralActionResponse>

    /**
     * Get the total deduction amount for a character.
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @return The total deduction amount (0.0 to 1.0)
     */
    fun getTotalDeduction(
        guildId: String,
        characterName: String,
    ): Double

    /**
     * Count behavioral actions for a guild.
     *
     * @param guildId The guild identifier
     * @return The count of behavioral actions for the guild
     */
    fun countByGuild(guildId: String): Long
}
