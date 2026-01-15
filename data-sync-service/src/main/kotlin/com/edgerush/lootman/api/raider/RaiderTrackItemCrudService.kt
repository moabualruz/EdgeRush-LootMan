package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface RaiderTrackItemCrudService : CrudService<Long, CreateRaiderTrackItemRequest, UpdateRaiderTrackItemRequest, RaiderTrackItemResponse> {
    fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderTrackItemResponse>

    fun countByRaiderId(raiderId: Long): Long
}
