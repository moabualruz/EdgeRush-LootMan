package com.edgerush.lootman.api.webhook

import java.time.Instant

/**
 * Payload received from WoWAudit webhook when a character is updated.
 *
 * This enables reactive sync - instead of polling, WoWAudit notifies us of changes.
 */
data class WoWAuditWebhookPayload(
    /** Type of event: "character.updated", "loot.awarded", etc. */
    val eventType: String,
    /** WoWAudit internal character ID (optional) */
    val characterId: Long? = null,
    /** Character name (required for sync lookup) */
    val characterName: String,
    /** Character realm (required for sync lookup) */
    val characterRealm: String,
    /** Guild ID this character belongs to */
    val guildId: String? = null,
    /** When the event occurred */
    val timestamp: Instant = Instant.now(),
) {
    /**
     * Converts this payload to a partial sync command.
     */
    fun toCommand() =
        PartialSyncCommand(
            characterName = characterName,
            characterRealm = characterRealm,
            guildId = guildId,
            eventType = eventType,
        )
}

/**
 * Command to trigger a partial sync for a single character.
 */
data class PartialSyncCommand(
    val characterName: String,
    val characterRealm: String,
    val guildId: String?,
    val eventType: String,
)

/**
 * Result of a partial sync operation.
 */
data class PartialSyncResult(
    val success: Boolean,
    val characterName: String,
    val message: String?,
    val syncRunId: Long?,
) {
    companion object {
        fun success(
            characterName: String,
            syncRunId: Long,
        ) = PartialSyncResult(
            success = true,
            characterName = characterName,
            message = "Character synced successfully",
            syncRunId = syncRunId,
        )

        fun failure(
            characterName: String,
            error: String?,
        ) = PartialSyncResult(
            success = false,
            characterName = characterName,
            message = error ?: "Unknown error",
            syncRunId = null,
        )
    }
}

/**
 * Response returned by the webhook endpoint.
 */
data class WebhookResponse(
    val accepted: Boolean,
    val message: String,
    val syncRunId: Long?,
) {
    companion object {
        fun from(result: PartialSyncResult) =
            WebhookResponse(
                accepted = result.success,
                message = result.message ?: "Processed",
                syncRunId = result.syncRunId,
            )
    }
}
