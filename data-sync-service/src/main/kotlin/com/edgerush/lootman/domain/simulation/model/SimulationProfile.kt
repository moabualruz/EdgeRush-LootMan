package com.edgerush.lootman.domain.simulation.model

import java.time.Instant

/**
 * Value object representing a SimulationCraft character profile.
 *
 * Contains the SimC-formatted profile content that can be used
 * to run gear upgrade simulations via Docker.
 */
@ConsistentCopyVisibility
data class SimulationProfile(
    val id: Long? = null,
    val guildId: String,
    val characterName: String,
    val characterRealm: String,
    val profileContent: String,
    val createdAt: Instant,
) {
    /**
     * Returns the character identifier in "Name-Realm" format.
     */
    val characterIdentifier: String
        get() = "$characterName-$characterRealm"

    companion object {
        /**
         * Creates a new SimulationProfile with validation.
         *
         * @param guildId The guild identifier
         * @param characterName The character name
         * @param characterRealm The realm name
         * @param profileContent The SimC profile content
         * @param createdAt When the profile was created
         * @return A validated SimulationProfile
         * @throws IllegalArgumentException if any field is blank
         */
        fun create(
            guildId: String,
            characterName: String,
            characterRealm: String,
            profileContent: String,
            createdAt: Instant,
        ): SimulationProfile {
            require(guildId.isNotBlank()) { "guildId must not be blank" }
            require(characterName.isNotBlank()) { "characterName must not be blank" }
            require(characterRealm.isNotBlank()) { "characterRealm must not be blank" }
            require(profileContent.isNotBlank()) { "profileContent must not be blank" }

            return SimulationProfile(
                id = null,
                guildId = guildId.trim(),
                characterName = characterName.trim(),
                characterRealm = characterRealm.trim(),
                profileContent = profileContent,
                createdAt = createdAt,
            )
        }
    }
}
