package com.edgerush.lootman.domain.shared.model

import com.edgerush.lootman.domain.shared.AccountId
import com.edgerush.lootman.domain.shared.CharacterId
import java.time.Instant

/**
 * A BaseCharacter is the simplest concrete implementation of WoWCharacter.
 *
 * This class represents a character identity record from the characters table,
 * without any additional guild or Battle.net specific properties.
 *
 * Use cases:
 * - Repository queries that only need character identity
 * - Characters that exist in the system but haven't been linked to Battle.net
 * - Temporary character records created during data import
 */
data class BaseCharacter(
    override val characterId: CharacterId,
    override val name: String,
    override val realm: String,
    override val region: String,
    override val characterClass: CharacterClass,
    override val blizzardId: Long?,
    override val accountId: AccountId?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
) : WoWCharacter(
        characterId = characterId,
        name = name,
        realm = realm,
        region = region,
        characterClass = characterClass,
        blizzardId = blizzardId,
        accountId = accountId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    ) {
    companion object {
        /**
         * Creates a new BaseCharacter with default timestamps.
         */
        fun create(
            characterId: CharacterId,
            name: String,
            realm: String,
            region: String,
            characterClass: CharacterClass,
            blizzardId: Long? = null,
            accountId: AccountId? = null,
        ): BaseCharacter {
            val now = Instant.now()
            return BaseCharacter(
                characterId = characterId,
                name = name,
                realm = realm,
                region = region,
                characterClass = characterClass,
                blizzardId = blizzardId,
                accountId = accountId,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
