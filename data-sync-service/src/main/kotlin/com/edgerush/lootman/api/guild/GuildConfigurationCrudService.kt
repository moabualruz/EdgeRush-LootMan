package com.edgerush.lootman.api.guild

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for GuildConfiguration operations.
 *
 * Extends the generic CrudService with guild-configuration-specific methods.
 */
interface GuildConfigurationCrudService : CrudService<Long, CreateGuildConfigurationRequest, UpdateGuildConfigurationRequest, GuildConfigurationResponse> {
    /**
     * Find a guild configuration by guild ID.
     *
     * @param guildId The guild identifier
     * @return The guild configuration
     * @throws NoSuchElementException if not found
     */
    fun findByGuildId(guildId: String): GuildConfigurationResponse

    /**
     * Find all active guild configurations with pagination.
     *
     * @param pageRequest Pagination parameters
     * @return Paginated list of active guild configurations
     */
    fun findActive(pageRequest: PageRequest): PagedResponse<GuildConfigurationResponse>

    /**
     * Update the benchmark configuration for a guild.
     *
     * @param id The guild configuration ID
     * @param request The benchmark update request
     * @return The updated guild configuration
     */
    fun updateBenchmark(
        id: Long,
        request: UpdateBenchmarkRequest,
    ): GuildConfigurationResponse

    /**
     * Update the sync status for a guild.
     *
     * @param guildId The guild identifier
     * @param status The sync status
     * @param error The error message if any
     * @return The updated guild configuration
     */
    fun updateSyncStatus(
        guildId: String,
        status: String,
        error: String?,
    ): GuildConfigurationResponse
}
