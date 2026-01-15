package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface RaiderRenownCrudService : CrudService<Long, CreateRaiderRenownRequest, UpdateRaiderRenownRequest, RaiderRenownResponse> {
    fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderRenownResponse>

    fun countByRaiderId(raiderId: Long): Long
}
