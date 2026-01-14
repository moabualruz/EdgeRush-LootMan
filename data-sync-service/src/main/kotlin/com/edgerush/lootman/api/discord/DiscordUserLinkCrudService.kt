package com.edgerush.lootman.api.discord

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for Discord user link operations.
 *
 * Provides CRUD operations plus Discord-specific query methods.
 */
interface DiscordUserLinkCrudService : CrudService<Long, CreateDiscordUserLinkRequest, UpdateDiscordUserLinkRequest, DiscordUserLinkResponse> {

    /**
     * Find all links for a Discord user.
     *
     * @param discordUserId The Discord user's ID
     * @return List of links for the user (may include alts)
     */
    fun findByDiscordUserId(discordUserId: String): List<DiscordUserLinkResponse>

    /**
     * Find the primary link for a Discord user.
     *
     * @param discordUserId The Discord user's ID
     * @return The primary link if one exists
     * @throws NoSuchElementException if no primary link exists
     */
    fun findPrimaryByDiscordUserId(discordUserId: String): DiscordUserLinkResponse

    /**
     * Find all links for a raider.
     *
     * @param raiderId The raider's ID
     * @return List of Discord users linked to this raider
     */
    fun findByRaiderId(raiderId: Long): List<DiscordUserLinkResponse>

    /**
     * Count links for a Discord user.
     *
     * @param discordUserId The Discord user's ID
     * @return The number of linked characters
     */
    fun countByDiscordUserId(discordUserId: String): Long

    /**
     * Set a link as the primary link for a Discord user.
     * Clears the primary flag from any existing primary link.
     *
     * @param linkId The link to set as primary
     * @return The updated link
     * @throws NoSuchElementException if link not found
     */
    fun setPrimary(linkId: Long): DiscordUserLinkResponse

    /**
     * Delete all links for a Discord user.
     *
     * @param discordUserId The Discord user's ID
     * @return The number of links deleted
     */
    fun deleteByDiscordUserId(discordUserId: String): Int
}
