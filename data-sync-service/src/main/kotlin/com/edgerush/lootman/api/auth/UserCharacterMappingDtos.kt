package com.edgerush.lootman.api.auth

import com.edgerush.lootman.domain.auth.model.UserCharacterMapping
import java.time.Instant

/**
 * Request to link a character to the current user.
 */
data class LinkCharacterRequest(
    val raiderId: Long,
    val isPrimary: Boolean = false
)

/**
 * Response for a user-character mapping.
 */
data class UserCharacterMappingResponse(
    val id: Long,
    val userId: Long,
    val raiderId: Long,
    val isPrimary: Boolean,
    val linkedAt: Instant,
    val verified: Boolean,
    val verifiedAt: Instant?
) {
    companion object {
        fun from(mapping: UserCharacterMapping): UserCharacterMappingResponse =
            UserCharacterMappingResponse(
                id = mapping.id!!.value,
                userId = mapping.userId.value,
                raiderId = mapping.raiderId.value,
                isPrimary = mapping.isPrimary,
                linkedAt = mapping.linkedAt,
                verified = mapping.verified,
                verifiedAt = mapping.verifiedAt
            )
    }
}

/**
 * Response containing the count of linked characters.
 */
data class CharacterCountResponse(
    val count: Long
)
