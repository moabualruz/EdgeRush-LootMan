package com.edgerush.lootman.api.team

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for TeamMetadata entity operations.
 *
 * Extends the generic CrudService with team-metadata-specific query methods.
 */
interface TeamMetadataCrudService : CrudService<Long, CreateTeamMetadataRequest, UpdateTeamMetadataRequest, TeamMetadataResponse> {

    /**
     * Find team metadata by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of team metadata for the guild
     */
    fun findByGuildId(guildId: Long, pageRequest: PageRequest): PagedResponse<TeamMetadataResponse>

    /**
     * Find team metadata by region with pagination.
     *
     * @param region The region (e.g., EU, US, KR)
     * @param pageRequest Pagination parameters
     * @return Paginated list of team metadata for the region
     */
    fun findByRegion(region: String, pageRequest: PageRequest): PagedResponse<TeamMetadataResponse>

    /**
     * Count team metadata for a guild.
     *
     * @param guildId The guild identifier
     * @return The count of team metadata for the guild
     */
    fun countByGuildId(guildId: Long): Long
}
