package com.edgerush.lootman.api.raidplan

import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import com.edgerush.lootman.application.raidplan.*
import com.edgerush.lootman.domain.raidplan.model.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for Raid Plan operations.
 * Provides endpoints for managing raid plans with canvas-based positioning.
 */
@RestController
@RequestMapping("/api/v1/raid-plans")
@Tag(name = "Raid Plans", description = "Raid plan management operations")
class RaidPlanController(
    private val raidPlanService: RaidPlanService,
    private val paginationProperties: PaginationProperties,
) {

    @Operation(summary = "Create a new raid plan")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Plan created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
    )
    @PostMapping
    fun createPlan(
        @Valid @RequestBody request: CreateRaidPlanApiRequest,
    ): ResponseEntity<RaidPlanResponse> {
        val plan = raidPlanService.createPlan(
            CreateRaidPlanRequest(
                guildId = request.guildId,
                encounterId = request.encounterId,
                encounterName = request.encounterName,
                name = request.name,
                createdBy = request.createdBy,
                visibility = request.visibility ?: PlanVisibility.GUILD
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(plan.toResponse())
    }

    @Operation(summary = "Get raid plan by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Plan found"),
        ApiResponse(responseCode = "404", description = "Plan not found"),
    )
    @GetMapping("/{id}")
    fun getPlan(
        @Parameter(description = "Plan ID")
        @PathVariable id: String,
    ): RaidPlanResponse = raidPlanService.getPlan(id).toResponse()

    @Operation(summary = "Get raid plan by share token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Plan found"),
        ApiResponse(responseCode = "404", description = "Plan not found"),
    )
    @GetMapping("/shared/{shareToken}")
    fun getPlanByShareToken(
        @Parameter(description = "Share token")
        @PathVariable shareToken: String,
    ): RaidPlanResponse = raidPlanService.getPlanByShareToken(shareToken).toResponse()

    @Operation(summary = "Get raid plans for a guild")
    @GetMapping("/guild/{guildId}")
    fun getPlansByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidPlanResponse> {
        val effectiveSize = size ?: paginationProperties.defaultPageSize
        val result = raidPlanService.getPlansByGuildPaginated(guildId, page, effectiveSize)
        return PagedResponse(
            content = result.content.map { it.toResponse() },
            page = result.page,
            size = result.size,
            totalElements = result.totalElements,
        )
    }

    @Operation(summary = "Get raid plans for a specific encounter")
    @GetMapping("/guild/{guildId}/encounter/{encounterId}")
    fun getPlansByEncounter(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Encounter ID")
        @PathVariable encounterId: Int,
    ): List<RaidPlanResponse> = raidPlanService.getPlansByEncounter(guildId, encounterId).map { it.toResponse() }

    @Operation(summary = "Update a raid plan")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Plan updated"),
        ApiResponse(responseCode = "404", description = "Plan not found"),
    )
    @PutMapping("/{id}")
    fun updatePlan(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateRaidPlanApiRequest,
    ): RaidPlanResponse {
        return raidPlanService.updatePlan(
            id,
            UpdateRaidPlanRequest(
                name = request.name,
                visibility = request.visibility
            )
        ).toResponse()
    }

    @Operation(summary = "Delete a raid plan")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Plan deleted"),
        ApiResponse(responseCode = "404", description = "Plan not found"),
    )
    @DeleteMapping("/{id}")
    fun deletePlan(
        @PathVariable id: String,
    ): ResponseEntity<Unit> {
        raidPlanService.deletePlan(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Add a step to the plan")
    @PostMapping("/{id}/steps")
    fun addStep(
        @PathVariable id: String,
        @RequestBody request: AddStepRequest,
    ): RaidPlanResponse = raidPlanService.addStep(id, request.notes).toResponse()

    @Operation(summary = "Remove a step from the plan")
    @DeleteMapping("/{id}/steps/{stepOrder}")
    fun removeStep(
        @PathVariable id: String,
        @PathVariable stepOrder: Int,
    ): RaidPlanResponse = raidPlanService.removeStep(id, stepOrder).toResponse()

    @Operation(summary = "Update a step's notes")
    @PutMapping("/{id}/steps/{stepOrder}")
    fun updateStep(
        @PathVariable id: String,
        @PathVariable stepOrder: Int,
        @RequestBody request: UpdateStepRequest,
    ): RaidPlanResponse = raidPlanService.updateStep(id, stepOrder, request.notes).toResponse()

    @Operation(summary = "Generate a share link for the plan")
    @PostMapping("/{id}/share")
    fun generateShareToken(
        @PathVariable id: String,
    ): ShareLinkResponse {
        val plan = raidPlanService.generateShareToken(id)
        return ShareLinkResponse(plan.shareToken!!)
    }

    @Operation(summary = "Revoke the share link")
    @DeleteMapping("/{id}/share")
    fun revokeShareToken(
        @PathVariable id: String,
    ): ResponseEntity<Unit> {
        raidPlanService.revokeShareToken(id)
        return ResponseEntity.noContent().build()
    }
}

// === API Request/Response DTOs ===

data class CreateRaidPlanApiRequest(
    val guildId: String,
    val encounterId: Int,
    val encounterName: String,
    val name: String,
    val createdBy: Long,
    val visibility: PlanVisibility? = null,
)

data class UpdateRaidPlanApiRequest(
    val name: String? = null,
    val visibility: PlanVisibility? = null,
)

data class AddStepRequest(
    val notes: String? = null,
)

data class UpdateStepRequest(
    val notes: String? = null,
)

data class ShareLinkResponse(
    val shareToken: String,
)

data class RaidPlanResponse(
    val id: String,
    val guildId: String,
    val encounterId: Int,
    val encounterName: String,
    val name: String,
    val steps: List<PlanStepResponse>,
    val visibility: PlanVisibility,
    val shareToken: String?,
    val createdBy: Long,
    val createdAt: String,
    val updatedAt: String,
)

data class PlanStepResponse(
    val order: Int,
    val notes: String?,
    val markers: List<PlanMarkerResponse>,
    val shapes: List<PlanShapeResponse>,
)

data class PlanMarkerResponse(
    val type: MarkerType,
    val x: Double,
    val y: Double,
    val label: String?,
    val color: String?,
)

data class PlanShapeResponse(
    val shapeType: ShapeType,
    val x1: Double,
    val y1: Double,
    val x2: Double?,
    val y2: Double?,
    val radius: Double?,
    val color: String?,
    val strokeWidth: Int,
)

// === Extension functions for mapping ===

fun RaidPlan.toResponse(): RaidPlanResponse = RaidPlanResponse(
    id = id,
    guildId = guildId.value,
    encounterId = encounterId,
    encounterName = encounterName,
    name = name,
    steps = steps.map { it.toResponse() },
    visibility = visibility,
    shareToken = shareToken,
    createdBy = createdBy,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun PlanStep.toResponse(): PlanStepResponse = PlanStepResponse(
    order = order,
    notes = notes,
    markers = markers.map { it.toResponse() },
    shapes = shapes.map { it.toResponse() },
)

fun PlanMarker.toResponse(): PlanMarkerResponse = PlanMarkerResponse(
    type = type,
    x = x,
    y = y,
    label = label,
    color = color,
)

fun PlanShape.toResponse(): PlanShapeResponse = PlanShapeResponse(
    shapeType = shapeType,
    x1 = x1,
    y1 = y1,
    x2 = x2,
    y2 = y2,
    radius = radius,
    color = color,
    strokeWidth = strokeWidth,
)
