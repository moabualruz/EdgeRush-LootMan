package com.edgerush.lootman.domain.application.repository

import com.edgerush.lootman.domain.application.model.Application
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.application.model.ApplicationStatus
import com.edgerush.lootman.domain.shared.GuildId

/**
 * Repository interface for the enhanced Application domain model.
 *
 * This repository works with the domain model directly, supporting
 * the enhanced recruitment system with OAuth and auto-fetch capabilities.
 */
interface EnhancedApplicationRepository {
    /**
     * Saves a new or updated application.
     */
    fun save(application: Application): Application

    /**
     * Finds an application by its ID.
     */
    fun findById(id: ApplicationId): Application?

    /**
     * Finds all applications for a guild.
     */
    fun findByGuildId(
        guildId: GuildId,
        offset: Long = 0,
        limit: Int = 50,
    ): List<Application>

    /**
     * Finds applications for a guild filtered by status.
     */
    fun findByGuildIdAndStatus(
        guildId: GuildId,
        status: ApplicationStatus,
        offset: Long = 0,
        limit: Int = 50,
    ): List<Application>

    /**
     * Finds an application by Discord ID within a guild.
     */
    fun findByGuildIdAndDiscordId(
        guildId: GuildId,
        discordId: String,
    ): Application?

    /**
     * Finds an application by Battle.net ID within a guild.
     */
    fun findByGuildIdAndBattleNetId(
        guildId: GuildId,
        battleNetId: String,
    ): Application?

    /**
     * Counts applications for a guild.
     */
    fun countByGuildId(guildId: GuildId): Long

    /**
     * Counts applications for a guild filtered by status.
     */
    fun countByGuildIdAndStatus(
        guildId: GuildId,
        status: ApplicationStatus,
    ): Long

    /**
     * Deletes an application by its ID.
     */
    fun deleteById(id: ApplicationId)

    /**
     * Checks if an application exists with the given ID.
     */
    fun existsById(id: ApplicationId): Boolean
}
