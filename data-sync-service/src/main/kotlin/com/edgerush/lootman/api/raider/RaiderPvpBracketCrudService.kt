package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface RaiderPvpBracketCrudService : CrudService<Long, CreateRaiderPvpBracketRequest, UpdateRaiderPvpBracketRequest, RaiderPvpBracketResponse> {
    fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderPvpBracketResponse>

    fun countByRaiderId(raiderId: Long): Long
}
