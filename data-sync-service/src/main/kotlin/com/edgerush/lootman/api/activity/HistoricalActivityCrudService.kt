package com.edgerush.lootman.api.activity

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface HistoricalActivityCrudService : CrudService<Long, CreateHistoricalActivityRequest, UpdateHistoricalActivityRequest, HistoricalActivityResponse> {
    fun findByCharacterId(
        characterId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<HistoricalActivityResponse>

    fun findByTeamId(
        teamId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<HistoricalActivityResponse>

    fun countByCharacterId(characterId: Long): Long

    fun countByTeamId(teamId: Long): Long
}
