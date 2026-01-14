package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface RaiderWarcraftLogCrudService : CrudService<Long, CreateRaiderWarcraftLogRequest, UpdateRaiderWarcraftLogRequest, RaiderWarcraftLogResponse> {
    fun findByRaiderId(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderWarcraftLogResponse>
    fun countByRaiderId(raiderId: Long): Long
    fun findByRaiderIdUnpaged(raiderId: Long, limit: Int): List<RaiderWarcraftLogResponse>
}
