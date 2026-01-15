package com.edgerush.lootman.domain.discord.model

import com.edgerush.lootman.domain.shared.RaiderId
import java.time.Instant

/**
 * Entity representing a link between a Discord user and a WoW raider character.
 *
 * A Discord user can have multiple links (for alts), but only one can be primary.
 * The link enables the Discord bot to identify which character a user is referring to
 * and allows the web frontend to authenticate users via Discord OAuth2.
 */
data class DiscordUserLink(
    val id: DiscordUserLinkId?,
    val discordUserId: DiscordUserId,
    val raiderId: RaiderId,
    val isPrimary: Boolean,
    val linkedAt: Instant,
    val linkedBy: String?,
) {
    /**
     * Creates a copy with a new ID assigned.
     */
    fun withId(newId: DiscordUserLinkId): DiscordUserLink = copy(id = newId)

    /**
     * Creates a copy marked as primary.
     */
    fun markAsPrimary(): DiscordUserLink = copy(isPrimary = true)

    /**
     * Creates a copy marked as non-primary.
     */
    fun markAsNonPrimary(): DiscordUserLink = copy(isPrimary = false)

    companion object {
        /**
         * Creates a new Discord user link.
         *
         * @param discordUserId The Discord user's ID
         * @param raiderId The raider's ID to link to
         * @param isPrimary Whether this is the user's primary character
         * @param linkedBy Who created the link (user's Discord tag or admin)
         */
        fun create(
            discordUserId: DiscordUserId,
            raiderId: RaiderId,
            isPrimary: Boolean = false,
            linkedBy: String? = null,
        ): DiscordUserLink =
            DiscordUserLink(
                id = null,
                discordUserId = discordUserId,
                raiderId = raiderId,
                isPrimary = isPrimary,
                linkedAt = Instant.now(),
                linkedBy = linkedBy,
            )
    }
}
