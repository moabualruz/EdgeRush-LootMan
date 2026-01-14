package com.edgerush.lootman.api.loot

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for LootBan operations.
 *
 * Extends the generic CrudService with loot-ban-specific query methods.
 */
interface LootBanCrudService : CrudService<Long, CreateLootBanEntityRequest, UpdateLootBanEntityRequest, LootBanResponse> {

    /**
     * Find loot bans by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of loot bans for the guild
     */
    fun findByGuild(guildId: String, pageRequest: PageRequest): PagedResponse<LootBanResponse>

    /**
     * Find active loot bans by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of active loot bans for the guild
     */
    fun findActiveByGuild(guildId: String, pageRequest: PageRequest): PagedResponse<LootBanResponse>

    /**
     * Check if a character is currently banned from loot.
     *
     * @param guildId The guild identifier
     * @param characterName The character name to check
     * @return true if the character is currently banned, false otherwise
     */
    fun isCharacterBanned(guildId: String, characterName: String): Boolean

    /**
     * Count loot bans for a guild.
     *
     * @param guildId The guild identifier
     * @return The count of loot bans for the guild
     */
    fun countByGuild(guildId: String): Long
}
