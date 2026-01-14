package com.edgerush.lootman.api.admin

import com.edgerush.lootman.api.behavioral.BehavioralActionCrudService
import com.edgerush.lootman.api.behavioral.CreateBehavioralActionRequest
import com.edgerush.lootman.api.behavioral.UpdateBehavioralActionRequest
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.loot.CreateLootBanEntityRequest
import com.edgerush.lootman.api.loot.LootBanCrudService
import com.edgerush.lootman.api.loot.UpdateLootBanEntityRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * REST controller for admin operations.
 *
 * Provides v1 API endpoints for behavioral actions and loot bans
 * with paths matching frontend expectations.
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Admin management endpoints")
class AdminController(
    private val behavioralActionService: BehavioralActionCrudService,
    private val lootBanService: LootBanCrudService,
) {
    // ==================== Behavioral Actions ====================

    @GetMapping("/guilds/{guildId}/behavioral-actions")
    @Operation(summary = "Get all behavioral actions for a guild")
    fun getBehavioralActions(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
    ): List<AdminBehavioralActionResponse> {
        val pageRequest = PageRequest.withDefaults(0, 1000, 50, 1000)
        val pagedResponse = behavioralActionService.findByGuild(guildId, pageRequest)
        return pagedResponse.content.map { AdminBehavioralActionResponse.from(it) }
    }

    @PostMapping("/guilds/{guildId}/behavioral-actions")
    @Operation(summary = "Create a new behavioral action")
    fun createBehavioralAction(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Valid @RequestBody request: AdminBehavioralActionRequest,
    ): AdminBehavioralActionResponse {
        val createRequest = CreateBehavioralActionRequest(
            guildId = guildId,
            characterName = request.characterName,
            actionType = request.actionType,
            deductionAmount = kotlin.math.abs(request.flpsModifier),
            reason = request.reason,
            appliedBy = "admin", // Will be replaced with actual user from security context
            expiresAt = request.endDate?.let { parseIsoDateTime(it) },
        )
        val response = behavioralActionService.create(createRequest)
        return AdminBehavioralActionResponse.from(response)
    }

    @PutMapping("/guilds/{guildId}/behavioral-actions/{actionId}")
    @Operation(summary = "Update an existing behavioral action")
    fun updateBehavioralAction(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Action ID")
        @PathVariable actionId: Long,
        @Valid @RequestBody request: AdminBehavioralActionUpdateRequest,
    ): AdminBehavioralActionResponse {
        val updateRequest = UpdateBehavioralActionRequest(
            reason = request.reason,
            deductionAmount = request.flpsModifier?.let { kotlin.math.abs(it) },
            expiresAt = request.endDate?.let { parseIsoDateTime(it) },
            isActive = request.active,
        )
        val response = behavioralActionService.update(actionId, updateRequest)
        return AdminBehavioralActionResponse.from(response)
    }

    @DeleteMapping("/guilds/{guildId}/behavioral-actions/{actionId}")
    @Operation(summary = "Delete a behavioral action")
    fun deleteBehavioralAction(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Action ID")
        @PathVariable actionId: Long,
    ): ResponseEntity<Unit> {
        behavioralActionService.delete(actionId)
        return ResponseEntity.noContent().build()
    }

    // ==================== Loot Bans ====================

    @GetMapping("/guilds/{guildId}/loot-bans")
    @Operation(summary = "Get all loot bans for a guild")
    fun getLootBans(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
    ): List<AdminLootBanResponse> {
        val pageRequest = PageRequest.withDefaults(0, 1000, 50, 1000)
        val pagedResponse = lootBanService.findByGuild(guildId, pageRequest)
        return pagedResponse.content.map { AdminLootBanResponse.from(it) }
    }

    @PostMapping("/guilds/{guildId}/loot-bans")
    @Operation(summary = "Create a new loot ban")
    fun createLootBan(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Valid @RequestBody request: AdminLootBanRequest,
    ): AdminLootBanResponse {
        val createRequest = CreateLootBanEntityRequest(
            guildId = guildId,
            characterName = request.characterName,
            reason = request.reason,
            bannedBy = "admin", // Will be replaced with actual user from security context
            expiresAt = request.endDate?.let { parseIsoDateTime(it) },
        )
        val response = lootBanService.create(createRequest)
        return AdminLootBanResponse.from(response)
    }

    @PutMapping("/guilds/{guildId}/loot-bans/{banId}")
    @Operation(summary = "Update an existing loot ban")
    fun updateLootBan(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Ban ID")
        @PathVariable banId: Long,
        @Valid @RequestBody request: AdminLootBanUpdateRequest,
    ): AdminLootBanResponse {
        val updateRequest = UpdateLootBanEntityRequest(
            reason = request.reason,
            expiresAt = request.endDate?.let { parseIsoDateTime(it) },
            isActive = request.active,
        )
        val response = lootBanService.update(banId, updateRequest)
        return AdminLootBanResponse.from(response)
    }

    @DeleteMapping("/guilds/{guildId}/loot-bans/{banId}")
    @Operation(summary = "Delete a loot ban")
    fun deleteLootBan(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Ban ID")
        @PathVariable banId: Long,
    ): ResponseEntity<Unit> {
        lootBanService.delete(banId)
        return ResponseEntity.noContent().build()
    }

    private fun parseIsoDateTime(isoString: String): LocalDateTime {
        return OffsetDateTime.parse(isoString).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
    }
}
