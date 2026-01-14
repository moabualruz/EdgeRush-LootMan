package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface RaiderCrestCountCrudService : CrudService<Long, CreateRaiderCrestCountRequest, UpdateRaiderCrestCountRequest, RaiderCrestCountResponse> {
    fun findByRaiderId(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderCrestCountResponse>
    fun countByRaiderId(raiderId: Long): Long
}
