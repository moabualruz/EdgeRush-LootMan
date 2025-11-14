package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaidRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidRequest
import com.edgerush.datasync.api.dto.response.RaidResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.application.raids.ScheduleRaidCommand
import com.edgerush.datasync.application.raids.ScheduleRaidUseCase
import com.edgerush.datasync.domain.raids.repository.RaidRepository
import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.infrastructure.persistence.mapper.RaidMapper as DomainRaidMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.crud.RaidCrudService
import com.edgerush.datasync.service.mapper.LegacyRaidMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raids")
@Tag(name = "Raids", description = "Manage raid events and schedules")
class RaidController(
    raidService: RaidCrudService,
    private val scheduleRaidUseCase: ScheduleRaidUseCase,
    private val raidRepository: RaidRepository,
    private val domainRaidMapper: DomainRaidMapper,
    private val raidMapper: LegacyRaidMapper,
) : BaseCrudController<RaidEntity, Long, CreateRaidRequest, UpdateRaidRequest, RaidResponse>(raidService) {
    private val raidService = raidService as RaidCrudService

    /**
     * Schedule a new raid using the new use case.
     * Overrides the base create method to use domain-driven design approach.
     */
    override fun create(
        @Valid @RequestBody request: CreateRaidRequest,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<RaidResponse> {
        // Convert request to command
        val command =
            ScheduleRaidCommand(
                guildId = user.guildIds.firstOrNull() ?: "default",
                scheduledDate = request.date,
                startTime = request.startTime,
                endTime = request.endTime,
                instance = request.instance,
                difficulty = request.difficulty,
                optional = request.optional ?: false,
                notes = request.notes,
            )

        // Execute use case
        val result = scheduleRaidUseCase.execute(command)

        return result.fold(
            onSuccess = { raid ->
                // Convert domain model to response
                val entity = domainRaidMapper.toEntity(raid)
                val response = raidMapper.toResponse(entity)
                ResponseEntity.status(HttpStatus.CREATED).body(response)
            },
            onFailure = { exception ->
                throw exception
            },
        )
    }

    /**
     * Get raid by ID.
     * Uses the new domain repository while maintaining backward compatibility.
     */
    override fun findById(
        @Parameter(description = "Entity ID")
        @PathVariable id: Long,
    ): ResponseEntity<RaidResponse> {
        val raidId = com.edgerush.datasync.domain.raids.model.RaidId(id)
        val raid =
            raidRepository.findById(raidId)
                ?: throw ResourceNotFoundException("Raid not found with id: $id")

        val entity = domainRaidMapper.toEntity(raid)
        val response = raidMapper.toResponse(entity)
        return ResponseEntity.ok(response)
    }

    /**
     * Get all raids for a specific team.
     * Maintains backward compatibility with existing endpoint.
     */
    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get all raids for a specific team")
    fun findByTeamId(
        @Parameter(description = "Team ID") @PathVariable teamId: Long,
    ): ResponseEntity<List<RaidResponse>> {
        val raids = raidService.findByTeamId(teamId)
        return ResponseEntity.ok(raids)
    }
}
