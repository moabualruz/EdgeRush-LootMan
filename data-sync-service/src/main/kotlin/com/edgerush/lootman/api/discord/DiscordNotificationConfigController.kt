package com.edgerush.lootman.api.discord

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for managing Discord notification configurations.
 */
@RestController
@RequestMapping("/api/v1/guilds/{guildId}/discord/config")
@Tag(name = "Discord Notifications", description = "Configure Discord notification channels per guild")
class DiscordNotificationConfigController(
    private val configService: DiscordNotificationConfigService
) {

    @GetMapping
    @Operation(
        summary = "Get notification configurations",
        description = "Returns all Discord notification configurations for a guild"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Configuration list returned")
    )
    fun getConfigs(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String
    ): GuildNotificationConfigsResponse {
        return configService.getConfigsForGuild(guildId)
    }

    @GetMapping("/{type}")
    @Operation(
        summary = "Get configuration by type",
        description = "Returns the notification configuration for a specific type"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Configuration returned"),
        ApiResponse(responseCode = "204", description = "No configuration for this type"),
        ApiResponse(responseCode = "400", description = "Invalid notification type")
    )
    fun getConfigByType(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Notification type (LOOT_AWARD, RDF_EXPIRY, PENALTY, LOOT_BAN, SYNC_COMPLETE)")
        @PathVariable type: String
    ): ResponseEntity<DiscordNotificationConfigResponse> {
        val config = configService.getConfigByType(guildId, type)
        return if (config != null) {
            ResponseEntity.ok(config)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @PutMapping
    @Operation(
        summary = "Create or update configuration",
        description = "Creates a new notification configuration or updates an existing one"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Configuration saved"),
        ApiResponse(responseCode = "400", description = "Invalid notification type")
    )
    fun upsertConfig(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @RequestBody request: UpsertNotificationConfigRequest
    ): DiscordNotificationConfigResponse {
        return configService.upsertConfig(guildId, request)
    }

    @PatchMapping("/{configId}")
    @Operation(
        summary = "Update configuration",
        description = "Updates an existing notification configuration"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Configuration updated"),
        ApiResponse(responseCode = "404", description = "Configuration not found")
    )
    fun updateConfig(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Configuration ID")
        @PathVariable configId: Long,
        @RequestBody request: UpdateNotificationConfigRequest
    ): DiscordNotificationConfigResponse {
        return configService.updateConfig(guildId, configId, request)
    }

    @DeleteMapping("/{configId}")
    @Operation(
        summary = "Delete configuration",
        description = "Deletes a notification configuration"
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Configuration deleted"),
        ApiResponse(responseCode = "404", description = "Configuration not found")
    )
    fun deleteConfig(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Configuration ID")
        @PathVariable configId: Long
    ): ResponseEntity<Unit> {
        configService.deleteConfig(guildId, configId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/test/{type}")
    @Operation(
        summary = "Test notification",
        description = "Sends a test notification to the configured channel"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Test result returned"),
        ApiResponse(responseCode = "400", description = "Invalid notification type")
    )
    fun testNotification(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Notification type to test")
        @PathVariable type: String
    ): TestNotificationResponse {
        return configService.testNotification(guildId, type)
    }
}
