package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaidRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidRequest
import com.edgerush.datasync.api.dto.response.RaidResponse
import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.service.crud.RaidCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raids")
@Tag(name = "Raids", description = "Manage raid events and schedules")
class RaidController(
    raidService: RaidCrudService,
) : BaseCrudController<RaidEntity, Long, CreateRaidRequest, UpdateRaidRequest, RaidResponse>(raidService) {
    private val raidService = raidService as RaidCrudService

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get all raids for a specific team")
    fun findByTeamId(
        @Parameter(description = "Team ID") @PathVariable teamId: Long,
    ): ResponseEntity<List<RaidResponse>> {
        val raids = raidService.findByTeamId(teamId)
        return ResponseEntity.ok(raids)
    }
}
