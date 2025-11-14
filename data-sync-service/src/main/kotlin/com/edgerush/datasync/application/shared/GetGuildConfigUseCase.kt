package com.edgerush.datasync.application.shared

import com.edgerush.datasync.domain.shared.model.Guild
import com.edgerush.datasync.domain.shared.model.GuildId
import com.edgerush.datasync.domain.shared.repository.GuildRepository
import org.springframework.stereotype.Service

/**
 * Use case for retrieving guild configuration.
 */
@Service
class GetGuildConfigUseCase(
    private val guildRepository: GuildRepository
) {
    /**
     * Get guild configuration by ID, creating default if not exists
     */
    fun execute(guildId: GuildId): Result<Guild> = runCatching {
        guildRepository.findActiveById(guildId)
            ?: createDefaultGuild(guildId)
    }
    
    /**
     * Get all active guilds
     */
    fun executeAll(): Result<List<Guild>> = runCatching {
        guildRepository.findAllActive()
    }
    
    private fun createDefaultGuild(guildId: GuildId): Guild {
        val guild = Guild.create(
            guildId = guildId.value,
            name = "Guild ${guildId.value}",
            description = "Auto-created guild configuration"
        )
        return guildRepository.save(guild)
    }
}

/**
 * Exception thrown when a guild is not found
 */
class GuildNotFoundException(guildId: GuildId) : Exception("Guild not found: ${guildId.value}")
