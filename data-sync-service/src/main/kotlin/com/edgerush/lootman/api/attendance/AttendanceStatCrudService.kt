package com.edgerush.lootman.api.attendance

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for AttendanceStat entity operations.
 *
 * Extends the generic CrudService with attendance-stat-specific query methods.
 */
interface AttendanceStatCrudService : CrudService<Long, CreateAttendanceStatRequest, UpdateAttendanceStatRequest, AttendanceStatResponse> {
    /**
     * Find attendance stats by character with pagination.
     *
     * @param characterId The character identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of attendance stats for the character
     */
    fun findByCharacterId(
        characterId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<AttendanceStatResponse>

    /**
     * Find attendance stats by team with pagination.
     *
     * @param teamId The team identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of attendance stats for the team
     */
    fun findByTeamId(
        teamId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<AttendanceStatResponse>

    /**
     * Find attendance stats by season with pagination.
     *
     * @param seasonId The season identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of attendance stats for the season
     */
    fun findBySeasonId(
        seasonId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<AttendanceStatResponse>

    /**
     * Count attendance stats for a character.
     *
     * @param characterId The character identifier
     * @return The count of attendance stats for the character
     */
    fun countByCharacterId(characterId: Long): Long
}
