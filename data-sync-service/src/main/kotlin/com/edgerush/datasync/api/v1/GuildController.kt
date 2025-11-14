package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.*
import com.edgerush.datasync.application.shared.*
import com.edgerush.datasync.domain.shared.model.BenchmarkMode
import com.edgerush.datasync.domain.shared.model.GuildId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for guild configuration management.
 */
@RestController
@RequestMapping("/api/v1/guilds")
class GuildController(
    private val getGuildConfigUseCase: GetGuildConfigUseCase,
    private val updateGuildConfigUseCase: UpdateGuildConfigUseCase
) {
    /**
     * Get all active guilds
     */
    @GetMapping
    fun getAllGuilds(): ResponseEntity<List<GuildDto>> {
        return getGuildConfigUseCase.executeAll()
            .map { guilds -> guilds.map { GuildDto.from(it) } }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.internalServerError().build() }
    }
    
    /**
     * Get guild configuration by ID
     */
    @GetMapping("/{guildId}")
    fun getGuildConfig(@PathVariable guildId: String): ResponseEntity<GuildDto> {
        return getGuildConfigUseCase.execute(GuildId(guildId))
            .map { GuildDto.from(it) }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.notFound().build() }
    }
    
    /**
     * Update guild benchmark configuration
     */
    @PatchMapping("/{guildId}/benchmark")
    fun updateBenchmark(
        @PathVariable guildId: String,
        @RequestBody request: UpdateBenchmarkRequest
    ): ResponseEntity<GuildDto> {
        val command = UpdateBenchmarkCommand(
            guildId = GuildId(guildId),
            mode = BenchmarkMode.valueOf(request.mode),
            customRms = request.customRms,
            customIpi = request.customIpi
        )
        
        return updateGuildConfigUseCase.execute(command)
            .map { GuildDto.from(it) }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.badRequest().build() }
    }
    
    /**
     * Update WoWAudit configuration
     */
    @PatchMapping("/{guildId}/wowaudit")
    fun updateWoWAuditConfig(
        @PathVariable guildId: String,
        @RequestBody request: UpdateWoWAuditConfigRequest
    ): ResponseEntity<GuildDto> {
        val command = UpdateWoWAuditConfigCommand(
            guildId = GuildId(guildId),
            apiKey = request.apiKey,
            guildUri = request.guildUri
        )
        
        return updateGuildConfigUseCase.execute(command)
            .map { GuildDto.from(it) }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.badRequest().build() }
    }
    
    /**
     * Enable or disable sync
     */
    @PatchMapping("/{guildId}/sync")
    fun setSyncEnabled(
        @PathVariable guildId: String,
        @RequestBody request: SetSyncEnabledRequest
    ): ResponseEntity<GuildDto> {
        val command = SetSyncEnabledCommand(
            guildId = GuildId(guildId),
            enabled = request.enabled
        )
        
        return updateGuildConfigUseCase.execute(command)
            .map { GuildDto.from(it) }
            .map { ResponseEntity.ok(it) }
            .getOrElse { ResponseEntity.badRequest().build() }
    }
}
