package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.RaiderDto
import com.edgerush.datasync.api.dto.UpdateRaiderStatusRequest
import com.edgerush.datasync.application.shared.GetRaiderUseCase
import com.edgerush.datasync.application.shared.UpdateRaiderStatusCommand
import com.edgerush.datasync.application.shared.UpdateRaiderUseCase
import com.edgerush.datasync.domain.shared.model.RaiderId
import com.edgerush.datasync.domain.shared.model.RaiderStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for raider management operations.
 */
@RestController
@RequestMapping("/api/v1/raiders")
class RaiderController(
    private val getRaiderUseCase: GetRaiderUseCase,
    private val updateRaiderUseCase: UpdateRaiderUseCase
) {
    /**
     * Get all raiders
     */
    @GetMapping
    fun getAllRaiders(): ResponseEntity<List<RaiderDto>> {
        return getRaiderUseCase.executeAll()
            .map { raiders -> raiders.map { RaiderDto.from(it) } }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.internalServerError().build() }
    }
    
    /**
     * Get all active raiders
     */
    @GetMapping("/active")
    fun getActiveRaiders(): ResponseEntity<List<RaiderDto>> {
        return getRaiderUseCase.executeAllActive()
            .map { raiders -> raiders.map { RaiderDto.from(it) } }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.internalServerError().build() }
    }
    
    /**
     * Get a raider by ID
     */
    @GetMapping("/{id}")
    fun getRaiderById(@PathVariable id: Long): ResponseEntity<RaiderDto> {
        return getRaiderUseCase.execute(RaiderId(id))
            .map { RaiderDto.from(it) }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.notFound().build() }
    }
    
    /**
     * Get a raider by character name and realm
     */
    @GetMapping("/character/{characterName}/realm/{realm}")
    fun getRaiderByCharacter(
        @PathVariable characterName: String,
        @PathVariable realm: String
    ): ResponseEntity<RaiderDto> {
        return getRaiderUseCase.execute(characterName, realm)
            .map { RaiderDto.from(it) }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.notFound().build() }
    }
    
    /**
     * Update raider status
     */
    @PatchMapping("/{id}/status")
    fun updateRaiderStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateRaiderStatusRequest
    ): ResponseEntity<RaiderDto> {
        val command = UpdateRaiderStatusCommand(
            raiderId = RaiderId(id),
            newStatus = RaiderStatus.fromString(request.status)
        )
        
        return updateRaiderUseCase.execute(command)
            .map { RaiderDto.from(it) }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.badRequest().build() }
    }
}
