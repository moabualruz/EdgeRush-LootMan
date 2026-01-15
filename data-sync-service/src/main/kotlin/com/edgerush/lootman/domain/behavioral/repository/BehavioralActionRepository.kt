package com.edgerush.lootman.domain.behavioral.repository

import com.edgerush.datasync.entity.BehavioralActionEntity

/**
 * Repository interface for BehavioralActionEntity CRUD operations.
 *
 * Provides access to behavioral action data.
 */
interface BehavioralActionRepository {
    /**
     * Finds a behavioral action by its unique identifier.
     *
     * @param id The behavioral action's unique identifier
     * @return The behavioral action entity if found, null otherwise
     */
    fun findById(id: Long): BehavioralActionEntity?

    /**
     * Checks if a behavioral action exists by ID.
     *
     * @param id The behavioral action's unique identifier
     * @return true if the behavioral action exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all behavioral actions with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of behavioral action entities
     */
    fun findAll(
        offset: Long,
        limit: Int,
    ): List<BehavioralActionEntity>

    /**
     * Counts all behavioral actions.
     *
     * @return The total count of behavioral actions
     */
    fun count(): Long

    /**
     * Finds behavioral actions by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of behavioral action entities for the guild
     */
    fun findByGuildId(
        guildId: String,
        offset: Long,
        limit: Int,
    ): List<BehavioralActionEntity>

    /**
     * Counts behavioral actions for a guild.
     *
     * @param guildId The guild identifier
     * @return The count of behavioral actions for the guild
     */
    fun countByGuildId(guildId: String): Long

    /**
     * Finds active behavioral actions by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of active behavioral action entities for the guild
     */
    fun findActiveByGuildId(
        guildId: String,
        offset: Long,
        limit: Int,
    ): List<BehavioralActionEntity>

    /**
     * Counts active behavioral actions for a guild.
     *
     * @param guildId The guild identifier
     * @return The count of active behavioral actions for the guild
     */
    fun countActiveByGuildId(guildId: String): Long

    /**
     * Finds behavioral actions by character with pagination.
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of behavioral action entities for the character
     */
    fun findByCharacter(
        guildId: String,
        characterName: String,
        offset: Long,
        limit: Int,
    ): List<BehavioralActionEntity>

    /**
     * Counts behavioral actions for a character.
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @return The count of behavioral actions for the character
     */
    fun countByCharacter(
        guildId: String,
        characterName: String,
    ): Long

    /**
     * Gets the total active deduction amount for a character.
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @return The total deduction amount (0.0 to 1.0)
     */
    fun getTotalActiveDeduction(
        guildId: String,
        characterName: String,
    ): Double

    /**
     * Saves a behavioral action entity.
     *
     * @param entity The behavioral action to save
     * @return The saved behavioral action entity
     */
    fun save(entity: BehavioralActionEntity): BehavioralActionEntity

    /**
     * Deletes a behavioral action by ID.
     *
     * @param id The behavioral action ID to delete
     */
    fun delete(id: Long)
}
