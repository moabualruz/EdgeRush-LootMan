package com.edgerush.lootman.domain.shared.model

import com.edgerush.lootman.domain.shared.AccountId
import com.edgerush.lootman.domain.shared.CharacterId
import java.time.Instant

/**
 * Abstract base class for all WoW character types.
 *
 * WoWCharacter represents the authoritative identity of a WoW character,
 * independent of guild membership. All performance data, attendance,
 * and other tracking data should be linked to CharacterId so that
 * data persists regardless of guild membership status.
 *
 * This class serves as the parent for:
 * - [Raider] - A character who is a member of a guild roster
 * - [TrackedCharacter] - A personal character tracked via Battle.net
 *
 * Named WoWCharacter to avoid conflict with java.lang.Character.
 */
abstract class WoWCharacter(
    /**
     * Unique identifier for this character across the system.
     */
    characterId: CharacterId,
    /**
     * Character name in WoW.
     */
    name: String,
    /**
     * Server/realm the character belongs to.
     */
    realm: String,
    /**
     * Region (EU, US, KR, TW, CN).
     */
    region: String,
    /**
     * The character's class (Warrior, Mage, etc.).
     */
    characterClass: CharacterClass,
    /**
     * Blizzard's unique ID for this character (from Battle.net API).
     * May be null if character hasn't been linked to Battle.net.
     */
    blizzardId: Long?,
    /**
     * Account ID that owns this character.
     * Used for account-level attendance aggregation.
     * May be null if character hasn't been linked to an account.
     */
    accountId: AccountId?,
    /**
     * When this character record was first created.
     */
    createdAt: Instant,
    /**
     * When this character record was last updated.
     */
    updatedAt: Instant,
) {
    // Validate before storing - using constructor parameters, not properties
    init {
        require(name.isNotBlank()) { "Character name cannot be blank" }
        require(realm.isNotBlank()) { "Realm cannot be blank" }
        require(region.isNotBlank()) { "Region cannot be blank" }
    }

    // Store validated values as properties
    open val characterId: CharacterId = characterId
    open val name: String = name
    open val realm: String = realm
    open val region: String = region
    open val characterClass: CharacterClass = characterClass
    open val blizzardId: Long? = blizzardId
    open val accountId: AccountId? = accountId
    open val createdAt: Instant = createdAt
    open val updatedAt: Instant = updatedAt

    /**
     * Unique character identity key combining name-realm-region.
     * This is the natural key for a WoW character.
     */
    val uniqueKey: String
        get() = "${name.lowercase()}-${realm.lowercase()}-${region.lowercase()}"

    /**
     * Display name for the character (Name-Realm).
     */
    val displayName: String
        get() = "$name-$realm"

    /**
     * Full identifier including region.
     */
    val fullIdentifier: String
        get() = "$name-$realm-${region.uppercase()}"

    /**
     * Checks if this character is linked to an account.
     */
    fun isLinkedToAccount(): Boolean = accountId != null

    /**
     * Checks if this character is linked to Battle.net.
     */
    fun isLinkedToBlizzard(): Boolean = blizzardId != null
}
