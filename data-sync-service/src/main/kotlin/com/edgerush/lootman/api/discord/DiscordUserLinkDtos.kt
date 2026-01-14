package com.edgerush.lootman.api.discord

import com.edgerush.lootman.domain.discord.model.DiscordUserLink
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.time.Instant

/**
 * Request DTO for creating a Discord user link.
 */
data class CreateDiscordUserLinkRequest(
    @field:NotBlank(message = "Discord user ID is required")
    @field:Pattern(regexp = "\\d{17,20}", message = "Discord user ID must be 17-20 digits")
    val discordUserId: String,

    @field:Positive(message = "Raider ID must be positive")
    val raiderId: Long,

    val isPrimary: Boolean = false,

    val linkedBy: String? = null
)

/**
 * Request DTO for updating a Discord user link.
 */
data class UpdateDiscordUserLinkRequest(
    val isPrimary: Boolean? = null,
    val linkedBy: String? = null
)

/**
 * Response DTO for a Discord user link.
 */
data class DiscordUserLinkResponse(
    val id: Long,
    val discordUserId: String,
    val raiderId: Long,
    val isPrimary: Boolean,
    val linkedAt: Instant,
    val linkedBy: String?
) {
    companion object {
        fun from(link: DiscordUserLink): DiscordUserLinkResponse = DiscordUserLinkResponse(
            id = link.id!!.value,
            discordUserId = link.discordUserId.value,
            raiderId = link.raiderId.value,
            isPrimary = link.isPrimary,
            linkedAt = link.linkedAt,
            linkedBy = link.linkedBy
        )
    }
}

/**
 * Response DTO for checking if a link exists.
 */
data class DiscordUserLinkExistsResponse(
    val exists: Boolean
)

/**
 * Response DTO for counting links.
 */
data class DiscordUserLinkCountResponse(
    val count: Long
)
