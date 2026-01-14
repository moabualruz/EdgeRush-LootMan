package com.edgerush.lootman.api.raid

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for RaidSignup operations.
 *
 * Extends the generic CrudService with raid-signup-specific query methods.
 */
interface RaidSignupCrudService : CrudService<Long, CreateRaidSignupRequest, UpdateRaidSignupRequest, RaidSignupResponse> {

    /**
     * Find signups by raid with pagination.
     *
     * @param raidId The raid identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of signups for the raid
     */
    fun findByRaid(raidId: Long, pageRequest: PageRequest): PagedResponse<RaidSignupResponse>

    /**
     * Find selected signups by raid with pagination.
     *
     * @param raidId The raid identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of selected signups for the raid
     */
    fun findSelectedByRaid(raidId: Long, pageRequest: PageRequest): PagedResponse<RaidSignupResponse>

    /**
     * Find signups by character with pagination.
     *
     * @param characterId The character identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of signups for the character
     */
    fun findByCharacter(characterId: Long, pageRequest: PageRequest): PagedResponse<RaidSignupResponse>

    /**
     * Count signups for a raid.
     *
     * @param raidId The raid identifier
     * @return The count of signups for the raid
     */
    fun countByRaid(raidId: Long): Long
}
