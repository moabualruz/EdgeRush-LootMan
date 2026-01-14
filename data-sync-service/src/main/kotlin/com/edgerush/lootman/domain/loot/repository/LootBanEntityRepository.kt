package com.edgerush.lootman.domain.loot.repository

import com.edgerush.datasync.entity.LootBanEntity

/**
 * Repository interface for LootBanEntity CRUD operations.
 *
 * Provides access to loot ban data at the entity level.
 */
interface LootBanEntityRepository {
    /**
     * Finds a loot ban by its unique identifier.
     *
     * @param id The loot ban's unique identifier
     * @return The loot ban entity if found, null otherwise
     */
    fun findById(id: Long): LootBanEntity?

    /**
     * Checks if a loot ban exists by ID.
     *
     * @param id The loot ban's unique identifier
     * @return true if the loot ban exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all loot bans with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of loot ban entities
     */
    fun findAll(offset: Long, limit: Int): List<LootBanEntity>

    /**
     * Counts all loot bans.
     *
     * @return The total count of loot bans
     */
    fun count(): Long

    /**
     * Finds loot bans by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of loot ban entities for the guild
     */
    fun findByGuildId(guildId: String, offset: Long, limit: Int): List<LootBanEntity>

    /**
     * Counts loot bans for a guild.
     *
     * @param guildId The guild identifier
     * @return The count of loot bans for the guild
     */
    fun countByGuildId(guildId: String): Long

    /**
     * Finds active loot bans by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of active loot ban entities for the guild
     */
    fun findActiveByGuildId(guildId: String, offset: Long, limit: Int): List<LootBanEntity>

    /**
     * Counts active loot bans for a guild.
     *
     * @param guildId The guild identifier
     * @return The count of active loot bans for the guild
     */
    fun countActiveByGuildId(guildId: String): Long

    /**
     * Checks if a character is currently banned from loot.
     *
     * @param guildId The guild identifier
     * @param characterName The character name to check
     * @return true if the character has an active ban, false otherwise
     */
    fun isCharacterBanned(guildId: String, characterName: String): Boolean

    /**
     * Saves a loot ban entity.
     *
     * @param lootBan The loot ban to save
     * @return The saved loot ban entity
     */
    fun save(lootBan: LootBanEntity): LootBanEntity

    /**
     * Deletes a loot ban by ID.
     *
     * @param id The loot ban ID to delete
     */
    fun delete(id: Long)
}
