package com.edgerush.lootman.api.webhook

import com.edgerush.lootman.application.sync.PartialSyncUseCase
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller for handling WoWAudit webhook events.
 *
 * This enables reactive sync - WoWAudit pushes changes to us instead of polling.
 * Endpoint: POST /api/v1/webhooks/wowaudit
 */
@RestController
@RequestMapping("/api/v1/webhooks/wowaudit")
class WoWAuditWebhookController(
    private val partialSyncUseCase: PartialSyncUseCase,
) {
    private val logger = LoggerFactory.getLogger(WoWAuditWebhookController::class.java)

    /**
     * Handles character update events from WoWAudit.
     *
     * @param payload The webhook payload containing character details
     * @param token The authentication token from WoWAudit (header: X-WoWAudit-Token)
     * @return 202 Accepted if processing started, 401 if auth fails
     */
    @PostMapping
    fun handleCharacterUpdate(
        @RequestBody payload: WoWAuditWebhookPayload,
        @RequestHeader("X-WoWAudit-Token", required = false) token: String?,
    ): ResponseEntity<WebhookResponse> {
        logger.info(
            "Received WoWAudit webhook: eventType={}, character={}-{}",
            payload.eventType,
            payload.characterName,
            payload.characterRealm,
        )

        // Token validation would be handled by a security filter in production
        // For now, we just log and process

        val result = partialSyncUseCase.execute(payload.toCommand())

        return if (result.success) {
            ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(WebhookResponse.from(result))
        } else {
            ResponseEntity.status(HttpStatus.ACCEPTED) // Still accepted, just failed to process
                .body(WebhookResponse.from(result))
        }
    }

    /**
     * Health check endpoint for webhook consumers.
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "healthy"))
    }
}
