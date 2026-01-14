package com.edgerush.lootman.domain.discord.repository

import com.edgerush.lootman.domain.discord.model.DiscordUserLink
import com.edgerush.lootman.domain.discord.model.DiscordUserLinkId
import com.edgerush.lootman.domain.discord.model.DiscordUserId
import com.edgerush.lootman.domain.shared.RaiderId

/**
 * Repository interface for Discord user link operations.
 *
 * Provides access to Discord-to-WoW character mappings used by
 * the Discord bot and web frontend for user identification.
 */
interface DiscordUserLinkRepository {

    /**
     * Finds a link by its unique identifier.
     *
     * @param id The link's unique identifier
     * @return The link if found, null otherwise
     */
    fun findById(id: DiscordUserLinkId): DiscordUserLink?

    /**
     * Finds all links for a Discord user.
     *
     * @param discordUserId The Discord user's ID
     * @return List of links for the user (may include alts)
     */
    fun findByDiscordUserId(discordUserId: DiscordUserId): List<DiscordUserLink>

    /**
     * Finds the primary link for a Discord user.
     *
     * @param discordUserId The Discord user's ID
     * @return The primary link if one exists, null otherwise
     */
    fun findPrimaryByDiscordUserId(discordUserId: DiscordUserId): DiscordUserLink?

    /**
     * Finds all links for a raider.
     *
     * @param raiderId The raider's ID
     * @return List of Discord users linked to this raider
     */
    fun findByRaiderId(raiderId: RaiderId): List<DiscordUserLink>

    /**
     * Checks if a specific link already exists.
     *
     * @param discordUserId The Discord user's ID
     * @param raiderId The raider's ID
     * @return true if the link exists, false otherwise
     */
    fun existsByDiscordUserIdAndRaiderId(discordUserId: DiscordUserId, raiderId: RaiderId): Boolean

    /**
     * Saves a Discord user link.
     *
     * @param link The link to save
     * @return The saved link with ID assigned
     */
    fun save(link: DiscordUserLink): DiscordUserLink

    /**
     * Deletes a link by its ID.
     *
     * @param id The link ID to delete
     */
    fun deleteById(id: DiscordUserLinkId)

    /**
     * Deletes all links for a Discord user.
     *
     * @param discordUserId The Discord user's ID
     * @return The number of links deleted
     */
    fun deleteByDiscordUserId(discordUserId: DiscordUserId): Int

    /**
     * Clears the primary flag for all links of a Discord user.
     *
     * Used before setting a new primary link.
     *
     * @param discordUserId The Discord user's ID
     */
    fun clearPrimaryForDiscordUser(discordUserId: DiscordUserId)

    /**
     * Counts all links for a Discord user.
     *
     * @param discordUserId The Discord user's ID
     * @return The number of linked characters
     */
    fun countByDiscordUserId(discordUserId: DiscordUserId): Long

    /**
     * Finds all links with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return Paginated list of links
     */
    fun findAll(offset: Long, limit: Int): List<DiscordUserLink>

    /**
     * Counts all links.
     *
     * @return The total number of links
     */
    fun count(): Long
}
