package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface RaiderRaidProgressCrudService : CrudService<Long, CreateRaiderRaidProgressRequest, UpdateRaiderRaidProgressRequest, RaiderRaidProgressResponse> {
    fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderRaidProgressResponse>

    fun countByRaiderId(raiderId: Long): Long
}
