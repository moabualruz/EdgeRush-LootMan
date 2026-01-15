package com.edgerush.lootman.domain.auth.model

import com.edgerush.lootman.domain.shared.RaiderId
import java.time.Instant

/**
 * Entity representing a mapping between an authenticated user and a WoW character.
 *
 * Users can link multiple characters (e.g., main + alts) with one designated as primary.
 */
data class UserCharacterMapping(
    val id: UserCharacterMappingId? = null,
    val userId: UserId,
    val raiderId: RaiderId,
    val isPrimary: Boolean = false,
    val linkedAt: Instant = Instant.now(),
    val verified: Boolean = false,
    val verifiedAt: Instant? = null,
) {
    /**
     * Creates a copy with the given ID.
     */
    fun withId(id: UserCharacterMappingId): UserCharacterMapping = copy(id = id)

    /**
     * Marks this mapping as the primary character.
     */
    fun markAsPrimary(): UserCharacterMapping = copy(isPrimary = true)

    /**
     * Marks this mapping as non-primary.
     */
    fun markAsNonPrimary(): UserCharacterMapping = copy(isPrimary = false)

    /**
     * Marks this character as verified (e.g., via Battle.net OAuth).
     */
    fun verify(): UserCharacterMapping =
        copy(
            verified = true,
            verifiedAt = Instant.now(),
        )

    companion object {
        /**
         * Creates a new user-character mapping.
         */
        fun create(
            userId: UserId,
            raiderId: RaiderId,
            isPrimary: Boolean = false,
        ): UserCharacterMapping =
            UserCharacterMapping(
                userId = userId,
                raiderId = raiderId,
                isPrimary = isPrimary,
            )
    }
}
