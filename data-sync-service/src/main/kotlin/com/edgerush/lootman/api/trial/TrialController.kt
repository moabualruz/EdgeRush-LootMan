package com.edgerush.lootman.api.trial

import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.trial.model.TrialId
import com.edgerush.lootman.domain.trial.model.TrialStatus
import com.edgerush.lootman.domain.trial.service.TrialService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing trial periods.
 */
@RestController
@RequestMapping("/api/trials")
@Tag(name = "Trials", description = "Trial period management")
class TrialController(
    private val trialService: TrialService,
) {
    @PostMapping
    @Operation(summary = "Create a new trial for an approved application")
    fun createTrial(@RequestBody request: CreateTrialRequest): ResponseEntity<TrialDto> {
        return try {
            val trial = trialService.createTrial(
                applicationId = ApplicationId(request.applicationId),
                guildId = GuildId(request.guildId),
                raidsRequired = request.raidsRequired,
                raiderId = request.raiderId,
            )
            ResponseEntity.status(HttpStatus.CREATED).body(TrialDto.from(trial))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @GetMapping("/{trialId}")
    @Operation(summary = "Get a trial by ID")
    fun getTrial(@PathVariable trialId: String): ResponseEntity<TrialDto> {
        val trial = trialService.getTrial(TrialId(trialId))
        return if (trial != null) {
            ResponseEntity.ok(TrialDto.from(trial))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    @Operation(summary = "List trials for a guild with optional status filter")
    fun listTrials(
        @RequestParam guildId: String,
        @RequestParam(required = false) status: TrialStatus?,
        @RequestParam(defaultValue = "0") offset: Long,
        @RequestParam(defaultValue = "50") limit: Int,
    ): ResponseEntity<TrialListResponse> {
        val guild = GuildId(guildId)

        val (trials, total) = if (status != null) {
            Pair(
                trialService.listTrialsByStatus(guild, status, offset, limit),
                trialService.countTrialsByStatus(guild, status),
            )
        } else {
            Pair(
                trialService.listTrials(guild, offset, limit),
                trialService.countTrials(guild),
            )
        }

        return ResponseEntity.ok(
            TrialListResponse(
                trials = trials.map { TrialDto.from(it) },
                total = total,
                offset = offset,
                limit = limit,
            ),
        )
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active and extended trials for a guild")
    fun getActiveTrials(@RequestParam guildId: String): ResponseEntity<List<TrialDto>> {
        val trials = trialService.getActiveTrials(GuildId(guildId))
        return ResponseEntity.ok(trials.map { TrialDto.from(it) })
    }

    @PostMapping("/{trialId}/metrics")
    @Operation(summary = "Update trial metrics based on raid participation")
    fun updateMetrics(
        @PathVariable trialId: String,
        @RequestBody request: UpdateMetricsRequest,
    ): ResponseEntity<TrialDto> {
        return try {
            val trial = trialService.updateMetrics(
                trialId = TrialId(trialId),
                raidsAttended = request.raidsAttended,
                attendanceRate = request.attendanceRate,
                averagePerformance = request.averagePerformance,
                deathsPerRaid = request.deathsPerRaid,
            )
            ResponseEntity.ok(TrialDto.from(trial))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/{trialId}/promote")
    @Operation(summary = "Promote a trial raider to full member")
    fun promoteTrial(
        @PathVariable trialId: String,
        @RequestBody request: PromoteTrialRequest,
    ): ResponseEntity<TrialDto> {
        return try {
            val trial = trialService.promoteTrial(
                trialId = TrialId(trialId),
                promoterId = request.promoterId,
                reason = request.reason,
            )
            ResponseEntity.ok(TrialDto.from(trial))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/{trialId}/extend")
    @Operation(summary = "Extend a trial period for additional evaluation")
    fun extendTrial(
        @PathVariable trialId: String,
        @RequestBody request: ExtendTrialRequest,
    ): ResponseEntity<TrialDto> {
        return try {
            val trial = trialService.extendTrial(
                trialId = TrialId(trialId),
                extenderId = request.extenderId,
                additionalRaids = request.additionalRaids,
                reason = request.reason,
            )
            ResponseEntity.ok(TrialDto.from(trial))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/{trialId}/end")
    @Operation(summary = "End a trial with a non-promotion outcome")
    fun endTrial(
        @PathVariable trialId: String,
        @RequestBody request: EndTrialRequest,
    ): ResponseEntity<TrialDto> {
        return try {
            val trial = trialService.endTrial(
                trialId = TrialId(trialId),
                officerId = request.officerId,
                outcome = request.outcome,
                reason = request.reason,
            )
            ResponseEntity.ok(TrialDto.from(trial))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/{trialId}")
    @Operation(summary = "Delete a trial")
    fun deleteTrial(@PathVariable trialId: String): ResponseEntity<Void> {
        trialService.deleteTrial(TrialId(trialId))
        return ResponseEntity.noContent().build()
    }
}
