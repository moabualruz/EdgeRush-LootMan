package com.edgerush.lootman.api.raid

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import java.time.LocalDate

/**
 * CRUD service interface for Raid operations.
 *
 * Extends the generic CrudService with raid-specific query methods.
 */
interface RaidCrudService : CrudService<Long, CreateRaidRequest, UpdateRaidRequest, RaidResponse> {

    /**
     * Find raids by team with pagination.
     *
     * @param teamId The team identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of raids for the team
     */
    fun findByTeam(teamId: Long, pageRequest: PageRequest): PagedResponse<RaidResponse>

    /**
     * Find raids within a date range with pagination.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param pageRequest Pagination parameters
     * @return Paginated list of raids within the date range
     */
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate, pageRequest: PageRequest): PagedResponse<RaidResponse>

    /**
     * Count raids for a team.
     *
     * @param teamId The team identifier
     * @return The count of raids for the team
     */
    fun countByTeam(teamId: Long): Long
}
