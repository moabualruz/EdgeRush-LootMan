package com.edgerush.datasync.application.shared

import com.edgerush.datasync.domain.shared.model.BenchmarkMode
import com.edgerush.datasync.domain.shared.model.Guild
import com.edgerush.datasync.domain.shared.model.GuildId
import com.edgerush.datasync.domain.shared.model.SyncStatus
import com.edgerush.datasync.domain.shared.repository.GuildRepository
import org.springframework.stereotype.Service

/**
 * Use case for updating guild configuration.
 */
@Service
class UpdateGuildConfigUseCase(
    private val guildRepository: GuildRepository
) {
    /**
     * Update guild benchmark configuration
     */
    fun execute(command: UpdateBenchmarkCommand): Result<Guild> = runCatching {
        val guild = guildRepository.findById(command.guildId)
            ?: throw GuildNotFoundException(command.guildId)
        
        val updated = guild.updateBenchmark(
            mode = command.mode,
            customRms = command.customRms,
            customIpi = command.customIpi
        )
        
        guildRepository.save(updated)
    }
    
    /**
     * Update guild WoWAudit configuration
     */
    fun execute(command: UpdateWoWAuditConfigCommand): Result<Guild> = runCatching {
        val guild = guildRepository.findById(command.guildId)
            ?: throw GuildNotFoundException(command.guildId)
        
        val updated = guild.updateWoWAuditConfig(
            apiKey = command.apiKey,
            guildUri = command.guildUri
        )
        
        guildRepository.save(updated)
    }
    
    /**
     * Update guild sync status
     */
    fun execute(command: UpdateSyncStatusCommand): Result<Guild> = runCatching {
        val guild = guildRepository.findById(command.guildId)
            ?: throw GuildNotFoundException(command.guildId)
        
        val updated = guild.updateSyncStatus(
            status = command.status,
            error = command.error
        )
        
        guildRepository.save(updated)
    }
    
    /**
     * Enable or disable guild sync
     */
    fun execute(command: SetSyncEnabledCommand): Result<Guild> = runCatching {
        val guild = guildRepository.findById(command.guildId)
            ?: throw GuildNotFoundException(command.guildId)
        
        val updated = guild.setSyncEnabled(command.enabled)
        guildRepository.save(updated)
    }
}

/**
 * Command to update benchmark configuration
 */
data class UpdateBenchmarkCommand(
    val guildId: GuildId,
    val mode: BenchmarkMode,
    val customRms: Double? = null,
    val customIpi: Double? = null
)

/**
 * Command to update WoWAudit configuration
 */
data class UpdateWoWAuditConfigCommand(
    val guildId: GuildId,
    val apiKey: String?,
    val guildUri: String?
)

/**
 * Command to update sync status
 */
data class UpdateSyncStatusCommand(
    val guildId: GuildId,
    val status: SyncStatus,
    val error: String? = null
)

/**
 * Command to enable/disable sync
 */
data class SetSyncEnabledCommand(
    val guildId: GuildId,
    val enabled: Boolean
)
