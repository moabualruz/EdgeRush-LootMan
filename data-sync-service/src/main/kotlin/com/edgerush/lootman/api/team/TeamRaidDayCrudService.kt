package com.edgerush.lootman.api.team

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for TeamRaidDay entity operations.
 *
 * Extends the generic CrudService with team-raid-day-specific query methods.
 */
interface TeamRaidDayCrudService : CrudService<Long, CreateTeamRaidDayRequest, UpdateTeamRaidDayRequest, TeamRaidDayResponse> {

    /**
     * Find team raid days by team with pagination.
     *
     * @param teamId The team identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of team raid days for the team
     */
    fun findByTeamId(teamId: Long, pageRequest: PageRequest): PagedResponse<TeamRaidDayResponse>

    /**
     * Count team raid days for a team.
     *
     * @param teamId The team identifier
     * @return The count of team raid days for the team
     */
    fun countByTeamId(teamId: Long): Long
}
