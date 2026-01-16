package com.edgerush.lootman.api.guild

import com.edgerush.lootman.application.guild.GuildContextService
import com.edgerush.lootman.domain.guild.model.GuildPermission
import com.edgerush.lootman.domain.guild.model.GuildPermissionId
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
import com.edgerush.lootman.domain.guild.repository.GuildPermissionRepository
import com.edgerush.lootman.domain.shared.GuildId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * REST controller for managing guild permissions.
 *
 * Allows guild officers to configure which ranks have which permissions.
 */
@RestController
@RequestMapping("/api/v1/guilds/{guildId}/permissions")
@Tag(name = "Guild Permissions", description = "Manage guild rank permissions")
class GuildPermissionController(
    private val guildPermissionRepository: GuildPermissionRepository,
    private val guildContextService: GuildContextService,
    private val userIdExtractor: UserIdExtractor,
) {
    @GetMapping
    @Operation(
        summary = "Get guild permissions",
        description = "Returns all permission configurations for a guild",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Permissions returned"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
        ApiResponse(responseCode = "403", description = "No permission to view guild settings"),
    )
    fun getPermissions(
        @PathVariable guildId: String,
        @RequestHeader("Authorization") authorization: String,
    ): ResponseEntity<List<GuildPermissionResponse>> {
        validateSettingsAccess(authorization, guildId)

        val permissions = guildPermissionRepository.findByGuildId(GuildId(guildId))
        return ResponseEntity.ok(permissions.map { it.toResponse() })
    }

    @GetMapping("/ranks")
    @Operation(
        summary = "Get ranks with permissions",
        description = "Returns all ranks that have any permissions configured",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Ranks returned"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
        ApiResponse(responseCode = "403", description = "No permission to view guild settings"),
    )
    fun getRanksWithPermissions(
        @PathVariable guildId: String,
        @RequestHeader("Authorization") authorization: String,
    ): ResponseEntity<List<String>> {
        validateSettingsAccess(authorization, guildId)

        val ranks = guildPermissionRepository.findDistinctRankNamesByGuildId(GuildId(guildId))
        return ResponseEntity.ok(ranks)
    }

    @PostMapping
    @Operation(
        summary = "Add permission",
        description = "Adds a permission for a rank in the guild",
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Permission created"),
        ApiResponse(responseCode = "400", description = "Invalid request"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
        ApiResponse(responseCode = "403", description = "No permission to modify guild settings"),
    )
    fun addPermission(
        @PathVariable guildId: String,
        @RequestHeader("Authorization") authorization: String,
        @Valid @RequestBody request: AddPermissionRequest,
    ): ResponseEntity<GuildPermissionResponse> {
        validateSettingsAccess(authorization, guildId)

        val permissionType =
            try {
                GuildPermissionType.valueOf(request.permissionType)
            } catch (e: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid permission type: ${request.permissionType}")
            }

        val permission =
            GuildPermission.create(
                guildId = GuildId(guildId),
                rankName = request.rankName,
                permissionType = permissionType,
            )

        val saved = guildPermissionRepository.save(permission)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved.toResponse())
    }

    @DeleteMapping("/{permissionId}")
    @Operation(
        summary = "Remove permission",
        description = "Removes a permission configuration",
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Permission deleted"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
        ApiResponse(responseCode = "403", description = "No permission to modify guild settings"),
        ApiResponse(responseCode = "404", description = "Permission not found"),
    )
    fun removePermission(
        @PathVariable guildId: String,
        @PathVariable permissionId: Long,
        @RequestHeader("Authorization") authorization: String,
    ): ResponseEntity<Void> {
        validateSettingsAccess(authorization, guildId)

        val permission = guildPermissionRepository.findById(GuildPermissionId(permissionId))
        if (permission == null || permission.guildId.value != guildId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")
        }

        guildPermissionRepository.deleteById(GuildPermissionId(permissionId))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/types")
    @Operation(
        summary = "Get permission types",
        description = "Returns all available permission types",
    )
    fun getPermissionTypes(): ResponseEntity<List<PermissionTypeInfo>> {
        val types =
            GuildPermissionType.entries.map {
                PermissionTypeInfo(
                    name = it.name,
                    description =
                        when (it) {
                            GuildPermissionType.SETTINGS_ACCESS -> "Access to guild settings page"
                            GuildPermissionType.LOOT_MANAGEMENT -> "Manage loot distribution"
                            GuildPermissionType.MEMBER_MANAGEMENT -> "Manage guild members"
                            GuildPermissionType.VIEW_ALL_SCORES -> "View all member FLPS scores"
                        },
                )
            }
        return ResponseEntity.ok(types)
    }

    private fun validateSettingsAccess(
        authorization: String,
        guildId: String,
    ) {
        val userId = userIdExtractor.extractUserId(authorization)
        if (!guildContextService.hasGuildPermission(userId, GuildId(guildId), GuildPermissionType.SETTINGS_ACCESS)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to access guild settings")
        }
    }

    private fun GuildPermission.toResponse() =
        GuildPermissionResponse(
            id = id!!.value,
            guildId = guildId.value,
            rankName = rankName,
            permissionType = permissionType.name,
            createdAt = createdAt.toString(),
        )
}

/**
 * Response DTO for guild permission.
 */
data class GuildPermissionResponse(
    val id: Long,
    val guildId: String,
    val rankName: String,
    val permissionType: String,
    val createdAt: String,
)

/**
 * Request DTO for adding a permission.
 */
data class AddPermissionRequest(
    @field:NotBlank(message = "Rank name is required")
    val rankName: String,
    @field:NotBlank(message = "Permission type is required")
    val permissionType: String,
)

/**
 * Info about a permission type.
 */
data class PermissionTypeInfo(
    val name: String,
    val description: String,
)
