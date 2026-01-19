package com.edgerush.lootman.domain.shared.repository

import com.edgerush.lootman.domain.shared.AccountId
import com.edgerush.lootman.domain.shared.CharacterId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.WoWCharacter

/**
 * Repository interface for WoWCharacter aggregate.
 *
 * Provides access to character identity data, independent of guild membership.
 * Characters are the authoritative source for character identity, with Raiders
 * being guild-specific views of characters.
 */
interface CharacterRepository {
    /**
     * Finds a character by their unique identifier.
     *
     * @param id The character's unique identifier
     * @return The character if found, null otherwise
     */
    fun findById(id: CharacterId): WoWCharacter?

    /**
     * Finds a character by their Blizzard ID.
     *
     * @param blizzardId The Blizzard-assigned unique ID
     * @return The character if found, null otherwise
     */
    fun findByBlizzardId(blizzardId: Long): WoWCharacter?

    /**
     * Finds a character by their natural key (name, realm, region).
     *
     * @param name The character name
     * @param realm The realm/server name
     * @param region The region (EU, US, etc.)
     * @return The character if found, null otherwise
     */
    fun findByNameRealmRegion(name: String, realm: String, region: String): WoWCharacter?

    /**
     * Finds all characters linked to an account.
     *
     * @param accountId The account ID
     * @return List of characters owned by the account
     */
    fun findByAccountId(accountId: AccountId): List<WoWCharacter>

    /**
     * Checks if a character exists by their natural key.
     *
     * @param name The character name
     * @param realm The realm/server name
     * @param region The region (EU, US, etc.)
     * @return true if the character exists
     */
    fun existsByNameRealmRegion(name: String, realm: String, region: String): Boolean

    /**
     * Gets or creates a character by their natural key.
     * If the character doesn't exist, creates a new one with the given class.
     *
     * @param name The character name
     * @param realm The realm/server name
     * @param region The region (EU, US, etc.)
     * @param characterClass The character class
     * @return The character ID (existing or newly created)
     */
    fun getOrCreateCharacterId(
        name: String,
        realm: String,
        region: String,
        characterClass: CharacterClass,
    ): CharacterId

    /**
     * Saves a character.
     *
     * @param character The character to save
     * @return The saved character
     */
    fun save(character: WoWCharacter): WoWCharacter

    /**
     * Links a character to an account.
     *
     * @param characterId The character to link
     * @param accountId The account to link to
     */
    fun linkToAccount(characterId: CharacterId, accountId: AccountId)

    /**
     * Links a character to their Blizzard ID.
     *
     * @param characterId The character to link
     * @param blizzardId The Blizzard ID from Battle.net API
     */
    fun linkToBlizzard(characterId: CharacterId, blizzardId: Long)
}
