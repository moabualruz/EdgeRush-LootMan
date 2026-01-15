package com.edgerush.lootman.domain.shared.repository

import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Raider

/**
 * Repository interface for Raider aggregate.
 *
 * Provides access to raider data across the guild.
 */
interface RaiderRepository {
    /**
     * Finds a raider by their unique identifier.
     *
     * @param id The raider's unique identifier
     * @return The raider if found, null otherwise
     */
    fun findById(id: RaiderId): Raider?

    /**
     * Finds all active raiders in a guild.
     *
     * @param guildId The guild's unique identifier
     * @return List of active raiders
     */
    fun findByGuildId(guildId: GuildId): List<Raider>

    /**
     * Finds raiders in a guild with pagination.
     *
     * @param guildId The guild's unique identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return Paginated list of raiders
     */
    fun findByGuildId(
        guildId: GuildId,
        offset: Long,
        limit: Int,
    ): List<Raider>

    /**
     * Counts total raiders in a guild.
     *
     * @param guildId The guild's unique identifier
     * @return The total count of raiders
     */
    fun countByGuildId(guildId: GuildId): Long

    /**
     * Finds a raider by character name and realm.
     *
     * @param characterName The character's name
     * @param realm The character's realm
     * @return The raider if found, null otherwise
     */
    fun findByCharacterNameAndRealm(
        characterName: String,
        realm: String,
    ): Raider?

    /**
     * Saves a raider.
     *
     * @param raider The raider to save
     * @return The saved raider
     */
    fun save(raider: Raider): Raider

    /**
     * Deletes a raider.
     *
     * @param id The raider ID to delete
     */
    fun delete(id: RaiderId)

    /**
     * Finds multiple raiders by their IDs.
     *
     * Used for batch loading to prevent N+1 queries.
     *
     * @param ids The list of raider IDs to find
     * @return List of found raiders (may be smaller than input if some IDs don't exist)
     */
    fun findByIds(ids: List<RaiderId>): List<Raider>
}
