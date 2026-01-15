package com.edgerush.lootman.api.raid

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for RaidEncounter operations.
 *
 * Extends the generic CrudService with raid-encounter-specific query methods.
 */
interface RaidEncounterCrudService : CrudService<Long, CreateRaidEncounterRequest, UpdateRaidEncounterRequest, RaidEncounterResponse> {
    /**
     * Find encounters by raid with pagination.
     *
     * @param raidId The raid identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of encounters for the raid
     */
    fun findByRaid(
        raidId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaidEncounterResponse>

    /**
     * Find enabled encounters by raid with pagination.
     *
     * @param raidId The raid identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of enabled encounters for the raid
     */
    fun findEnabledByRaid(
        raidId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaidEncounterResponse>

    /**
     * Count encounters for a raid.
     *
     * @param raidId The raid identifier
     * @return The count of encounters for the raid
     */
    fun countByRaid(raidId: Long): Long
}
